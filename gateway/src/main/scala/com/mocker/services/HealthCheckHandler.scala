package com.mocker.services

import zio.ZIO
import zio.http.model.Method.GET
import zio.http._

object HealthCheckHandler {

  def routes: Http[Any, Nothing, Request, Response] = Http.collectZIO[Request] {
    case req @ GET -> !! / "ping" =>
      for {
        _ <- ZIO.unit
      } yield Response.text("pong")
  }
}
