package com.mocker.rest.backend

import com.mocker.common.utils.Database
import com.mocker.rest.application.RestMockerConfig

trait RestMockerBackendBuilder {

  def build(c: RestMockerConfig): RestMockerBackend = {
    val restMockerDatabase = Database.connect(c.mockerDatabaseConfig)
    RestMockerBackend()
  }
}
