package com.mocker.rest.manager

import com.mocker.rest.dao.ServiceActions
import com.mocker.rest.dao.mysql.MySqlServiceActions
import com.mocker.rest.errors.ServiceExistsException
import com.mocker.rest.model.Service
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{ZIO, ZLayer}

import scala.concurrent.ExecutionContext
case class RestMockerManager(restMockerDbProvider: DatabaseProvider, serviceActions: ServiceActions) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def createService(service: Service): ZIO[Any, Throwable, Unit] = {
    for {
      _ <- checkServiceNotExists(service)
      _ <- ZIO.fromDBIO(serviceActions.upsert(service)).provide(dbLayer)
    } yield ()
  }

  private def checkServiceNotExists(service: Service) = {
    for {
      dbService <- ZIO.fromDBIO(serviceActions.get(service.path)).provide(dbLayer)
      _ <- if (dbService.isDefined)
        ZIO.fail(ServiceExistsException(service.path))
      else
        ZIO.succeed()
    } yield ()
  }
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
