package com.mocker.gateway.routes

import zhttp.http.Method.GET
import zhttp.http._
import zio.ZIO

object HealthCheckHandler {
  val prefix: Path = !! / "ping"

  def routes: Http[Any, Nothing, Request, Response] = Http.collectZIO[Request] {
    case req @ GET -> prefix =>
      for {
        _ <- ZIO.unit
      } yield Response.text("pong")
  }
}
