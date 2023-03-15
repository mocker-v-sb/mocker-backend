package com.mocker.gateway.routes

import zhttp.http.Method.{GET, POST}
import zhttp.http._
import zio.ZIO

object MockRestApiHandler {
  val prefix: Path = !! / "rest"

  lazy val routes: Http[Any, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req @ POST -> prefix =>
      for {
        _ <- ZIO.unit
      } yield Response.text("Created mock Rest endpoint")
    case req @ GET -> prefix /: path =>
      for {
        _ <- ZIO.unit
      } yield Response.text(s"Path: $path")
  }
}
