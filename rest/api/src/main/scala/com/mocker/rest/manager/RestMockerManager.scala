package com.mocker.rest.manager

import com.mocker.rest.dao.ServiceActions
import com.mocker.rest.dao.mysql.MySqlServiceActions
import slick.interop.zio.DatabaseProvider
import zio.{ZIO, ZLayer}

import scala.concurrent.ExecutionContext
case class RestMockerManager(restMockerDbProvider: DatabaseProvider, act: ServiceActions) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)
}

object RestMockerManager {

  def layer(implicit ec: ExecutionContext): ZLayer[DatabaseProvider, Nothing, RestMockerManager] = {
    ZLayer.fromZIO {
      for {
        restMockerDatabase <- ZIO.service[DatabaseProvider]
      } yield RestMockerManager(restMockerDatabase, MySqlServiceActions())
    }
  }
}
