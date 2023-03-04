package com.mocker.common.utils

import com.typesafe.config.Config
import slick.jdbc.JdbcBackend.{Database => SlickDatabase}

object Database {

  def connect(config: Config): SlickDatabase = SlickDatabase.forConfig("", config)
}
