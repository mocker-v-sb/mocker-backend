package com.mocker.gateway

import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.gateway.routes._
import com.mocker.mq.mq_service.ZioMqService.MqMockerClient
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import zhttp.http._
import zhttp.service.Server
import zio._

object Main extends ZIOAppDefault {

  val serverAddress: ServerAddress =
    ServerAddress(Environment.conf.getString("gateway-server.address"), Environment.conf.getInt("gateway-server.port"))

  val routes: Http[MqMockerClient.Service with RestMockerClient.Service, Throwable, Request, Response] =
    HealthCheckHandler.routes ++ MockMqHandler.routes

  val program: ZIO[Any, Throwable, ExitCode] = for {
    _ <- Console.printLine(s"Starting server on $serverAddress")
    _ <- Server.start(serverAddress.port, routes)
  } yield ExitCode.success

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = program
}
