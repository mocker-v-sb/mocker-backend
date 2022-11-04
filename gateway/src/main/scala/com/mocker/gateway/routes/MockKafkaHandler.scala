package com.mocker.gateway.routes

import zhttp.http.Method.{GET, POST}
import zhttp.http._

object MockKafkaHandler extends Handler {
  val prefix: Path = !! / "kafka"

  lazy val routes: Http[Any, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req @ POST -> prefix =>
      for {
        _ <- logRequestInfo(req)
      } yield Response.text("Created new broker. Credentials: {}")
    case req @ GET -> prefix =>
      for {
        _ <- logRequestInfo(req)
      } yield Response.text("Broker is running. Credentials: {}")
  }
}
