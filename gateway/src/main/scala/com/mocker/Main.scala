package com.mocker

import com.mocker.common.monitoring.tracing.JaegerTracer
import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.mq.mq_service.ZioMqService.MqMockerClient
import com.mocker.repository.impls.{AuthRepositoryImpl, RefreshTokenRepositoryImpl}
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import com.mocker.services.{AuthenticationService, GraphQlMockerManager, MqMockerManager}
import com.mocker.services.rest._
import io.grpc.ManagedChannelBuilder
import io.opentelemetry.api.trace.Tracer
import scalapb.zio_grpc.ZManagedChannel
import zio._
import zio.http.HttpAppMiddleware.{bearerAuth, cors}
import zio.http._
import zio.http.middleware.Cors.CorsConfig
import zio.http.model.Method
import zio.sql.ConnectionPool
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.tracing.Tracing

import java.util.Properties

object Main extends ZIOAppDefault {

  val corsConfig: CorsConfig = CorsConfig(
    anyOrigin = true,
    anyMethod = true,
    allowedOrigins = _ => true,
    allowedMethods = Some(Set(Method.GET, Method.POST, Method.PUT, Method.PATCH, Method.DELETE))
  )

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

  val tracer: ZLayer[Any, Throwable, Tracer] =
    JaegerTracer.live("mocker-gateway", ServerAddress("158.160.57.255", 14250))

  val restMockerRoutes = MockRestApiServiceHandler.routes ++ MockRestApiModelHandler.routes ++
    MockRestApiMockHandler.routes ++ MockRestApiMockResponseHandler.routes

  val serverConfig: ServerConfig => ServerConfig = _.port(9000)

  val routes = for {
    authService <- ZIO.service[AuthenticationService]
    graphqlMockerManager <- ZIO.service[GraphQlMockerManager]
    mqMockerManager <- ZIO.service[MqMockerManager]
  } yield (authService.routes ++
    (graphqlMockerManager.protectedRoutes ++ mqMockerManager.routes ++ restMockerRoutes) @@
      bearerAuth(AuthenticationService.jwtDecode(_).isDefined) ++
    MockRestHandler.routes ++ graphqlMockerManager.routes) @@ cors(corsConfig)

  val connectionPoolConfig = ZLayer.succeed {
    zio.sql.ConnectionPoolConfig(
      url = Environment.conf.getString("auth-db.url"),
      properties = {
        val props = new Properties
        props.setProperty("user", Environment.conf.getString("auth-db.user"))
        props.setProperty("password", Environment.conf.getString("auth-db.password"))
        props
      }
    )
  }
  val connectionPool = connectionPoolConfig >>> ConnectionPool.live
  val authRepositoryLayer = connectionPool >>> AuthRepositoryImpl.live
  val refreshTokenRepositoryLayer = connectionPool >>> RefreshTokenRepositoryImpl.live

  val program: ZIO[Any, Throwable, ExitCode] = for {
    routes <- routes.provide(
      authRepositoryLayer,
      refreshTokenRepositoryLayer,
      AuthenticationService.live,
      GraphQlMockerManager.live,
      MqMockerManager.live,
      Tracing.propagating,
      tracer,
      ContextStorage.fiberRef
    )
    _ <- Server
      .serve(routes)
      .provide(
        authRepositoryLayer,
        refreshTokenRepositoryLayer,
        Client.default,
        Server.defaultWith(serverConfig),
        mqMockerClient,
        restMockerClient
      )
  } yield ExitCode.success

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = program
}
