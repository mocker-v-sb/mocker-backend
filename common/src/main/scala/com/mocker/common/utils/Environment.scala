package com.mocker.common.utils

import com.typesafe.config.{Config, ConfigFactory}

object Environment {
  lazy val conf: Config = ConfigFactory.load()
}
