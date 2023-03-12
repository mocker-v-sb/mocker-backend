package com.mocker.rest.manager

import com.mocker.rest.dao.{ModelActions, ServiceActions}
import com.mocker.rest.dao.mysql.{MySqlModelActions, MySqlServiceActions}
import com.mocker.rest.errors.{ServiceExistsException, ServiceNotExistsException}
import com.mocker.rest.model.{Model, Service}
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{ZIO, ZLayer}

import scala.concurrent.ExecutionContext
case class RestMockerManager(
    restMockerDbProvider: DatabaseProvider,
    serviceActions: ServiceActions,
    modelActions: ModelActions
) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def createService(service: Service): ZIO[Any, Throwable, Unit] = {
    for {
      _ <- checkServiceNotExists(service)
      _ <- ZIO.fromDBIO(serviceActions.upsert(service)).provide(dbLayer)
    } yield ()
  }

  def createModel(servicePath: String, model: Model): ZIO[Any, Throwable, Unit] = {
    for {
      serviceId <- checkServiceExists(servicePath)
      _ <- ZIO.fromDBIO(modelActions.upsert(model.copy(serviceId = serviceId))).provide(dbLayer)
    } yield ()
  }

  private def checkServiceExists(path: String) = {
    for {
      dbService <- ZIO.fromDBIO(serviceActions.get(path)).provide(dbLayer)
      serviceId <- dbService match {
        case Some(service) => ZIO.succeed(service.id)
        case None          => ZIO.fail(ServiceNotExistsException(path)) //
      }
    } yield serviceId
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
      } yield RestMockerManager(restMockerDatabase, MySqlServiceActions(), MySqlModelActions())
    }
  }
}
