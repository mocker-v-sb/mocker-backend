package com.mocker.gateway

import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.gateway.routes.AuthenticationHandler.jwtDecode
import com.mocker.gateway.routes._
import com.mocker.gateway.routes.rest._
import com.mocker.mq.mq_service.ZioMqService.MqMockerClient
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import io.grpc.ManagedChannelBuilder
import scalapb.zio_grpc.ZManagedChannel
import zio._
import zio.http.HttpAppMiddleware.{bearerAuth, cors}
import zio.http._
import zio.http.middleware.Cors.CorsConfig
import zio.http.model.Method

object Main extends ZIOAppDefault {

  val corsConfig: CorsConfig = CorsConfig(
    anyOrigin = true,
    anyMethod = true,
    allowedOrigins = _ => true,
    allowedMethods = Some(Set(Method.GET, Method.POST, Method.PUT, Method.PATCH, Method.DELETE))
  )

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

  val routes =
    (AuthenticationHandler.routes ++ (MockMqHandler.routes ++ MockRestApiServiceHandler.routes ++
      MockRestApiModelHandler.routes ++ MockRestApiMockHandler.routes ++
      MockRestApiMockResponseHandler.routes ++ MockRestHandler.routes ++
      GraphQlMockerHandler.routes) @@ bearerAuth(jwtDecode(_).isDefined)) @@ cors(corsConfig)

  val program: ZIO[Any, Throwable, ExitCode] = for {
    _ <- Console.printLine(s"Starting server on $serverAddress")
    _ <- Server.serve(routes).provideLayer(mqMockerClient ++ restMockerClient ++ Server.default ++ Client.default)
  } yield ExitCode.success

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = program
}
