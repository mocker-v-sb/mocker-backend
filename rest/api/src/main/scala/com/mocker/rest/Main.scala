package com.mocker.rest

import com.mocker.rest.application.RestMockerConfig
import com.mocker.rest.backend.RestMockerBackendBuilder
import com.typesafe.config.ConfigFactory
import zio.{Scope, ZIO, ZIOAppArgs}

object Main extends zio.ZIOAppDefault with RestMockerBackendBuilder {

  private val backend = build(RestMockerConfig(ConfigFactory.load()))
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = ???
}
