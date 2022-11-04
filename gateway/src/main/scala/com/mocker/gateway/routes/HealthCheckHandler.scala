package com.mocker.gateway.routes

import zhttp.http.Method.GET
import zhttp.http._

object HealthCheckHandler extends Handler {
  val prefix: Path = !! / "ping"

  def routes: Http[Any, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req @ GET -> prefix =>
      for {
        _ <- logRequestInfo(req)
      } yield Response.text("pong")
  }
}
