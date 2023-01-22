package com.mocker.core

import com.typesafe.config.{Config, ConfigFactory}

object Environment {
  lazy val conf: Config = ConfigFactory.load()

  lazy val isProduction: Boolean = conf.getBoolean("environment.isProduction")
}