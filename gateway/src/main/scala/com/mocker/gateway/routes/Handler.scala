package com.mocker.gateway.routes

import com.mocker.gateway.Environment
import zhttp.http.{Http, Path, Request, Response}
import zio.ZIO

trait Handler {
  val prefix: Path

  def routes: Http[Any, Throwable, Request, Response]

  def logRequestInfo(req: Request): ZIO[Any, Throwable, Unit] = {
    for {
      path <- ZIO.attempt(req.path.toString())
      method <- ZIO.attempt(req.method.toString())
      body <- req.bodyAsString
    } yield {
      if (!Environment.isProduction) println(s"Path: $path\nMethod: $method\nBody: $body")
    }
  }
}