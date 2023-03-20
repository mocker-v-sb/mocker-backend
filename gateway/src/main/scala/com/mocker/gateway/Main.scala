package com.mocker.gateway

import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.gateway.routes._
import com.mocker.gateway.routes.rest.{MockRestApiModelHandler, MockRestApiServiceHandler}
import com.mocker.mq.mq_service.ZioMqService.MqMockerClient
import com.mocker.mq.mq_service.ZioMqService.MqMockerClient.Service
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient.Service
import io.grpc.ManagedChannelBuilder
import scalapb.zio_grpc.ZManagedChannel
import zhttp.http._
import zhttp.service.Server
import zio._

object Main extends ZIOAppDefault {

  val serverAddress: ServerAddress =
    ServerAddress(Environment.conf.getString("gateway-server.address"), Environment.conf.getInt("gateway-server.port"))

  val mqMockerClient: Layer[Throwable, MqMockerClient.Service] =
    MqMockerClient.live(
      ZManagedChannel(
        ManagedChannelBuilder
          .forAddress(
            Environment.conf.getString("mq-mocker-server.address"),
            Environment.conf.getInt("mq-mocker-server.port")
          )
          .usePlaintext()
      )
    )

  val restMockerClient: Layer[Throwable, RestMockerClient.Service] =
    RestMockerClient.live(
      ZManagedChannel(
        ManagedChannelBuilder
          .forAddress(
            Environment.conf.getString("rest-mocker-server.address"),
            Environment.conf.getInt("rest-mocker-server.port")
          )
          .usePlaintext()
      )
    )

  val routes: Http[MqMockerClient.Service with RestMockerClient.Service, Throwable, Request, Response] =
    MockMqHandler.routes ++ MockRestApiServiceHandler.routes ++ MockRestApiModelHandler.routes

  val program: ZIO[Any, Throwable, ExitCode] = for {
    _ <- Console.printLine(s"Starting server on $serverAddress")
    _ <- Server.start(serverAddress.port, routes).provideLayer(mqMockerClient ++ restMockerClient)
  } yield ExitCode.success

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = program
}
