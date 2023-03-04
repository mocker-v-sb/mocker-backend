package com.mocker.rest.manager

import slick.jdbc.JdbcBackend.{Database => SlickDatabase}
import zio.{ZIO, ZLayer}
case class RestMockerManager(restMockerDatabase: SlickDatabase) {}

object RestMockerManager {

  def layer: ZLayer[SlickDatabase, Nothing, RestMockerManager] = {
    ZLayer.fromZIO {
      for {
        restMockerDatabase <- ZIO.service[SlickDatabase]
      } yield RestMockerManager(restMockerDatabase)
    }
  }
}
