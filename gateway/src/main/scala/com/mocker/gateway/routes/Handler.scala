package com.mocker.gateway.routes

import zhttp.http.{Http, Path, Request, Response}

trait Handler {
  val prefix: Path

  def routes: Http[Any, Throwable, Request, Response]
}
