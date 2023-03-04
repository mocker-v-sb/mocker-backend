package com.mocker.rest.application

import com.typesafe.config.Config

case class RestMockerConfig(mockerDatabaseConfig: Config)

object RestMockerConfig {

  def apply(c: Config): RestMockerConfig = {
    new RestMockerConfig(
      mockerDatabaseConfig = c.getConfig("mysql.rest")
    )
  }

}
