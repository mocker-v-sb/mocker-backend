package com.mocker.gateway

import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.gateway.routes._
import zhttp.http._
import zhttp.service.Server
import zio._

object Main extends ZIOAppDefault {

  val serverAddress: ServerAddress =
    ServerAddress(Environment.conf.getString("gateway-server.address"), Environment.conf.getInt("gateway-server.port"))

  val handlers: Seq[Handler] = Seq(
    MockRestApiHandler,
    MockKafkaHandler
  )

  val routes: Http[Any, Throwable, Request, Response] = handlers.foldLeft(HealthCheckHandler.routes)(_ ++ _.routes)

  val program: ZIO[Any, Throwable, ExitCode] = for {
    _ <- Console.printLine(s"Starting server on $serverAddress")
    _ <- Server.start(serverAddress.port, routes)
  } yield ExitCode.success

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = program
}
