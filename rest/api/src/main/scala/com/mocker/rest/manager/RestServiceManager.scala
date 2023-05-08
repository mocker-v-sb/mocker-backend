package com.mocker.rest.manager

import com.mocker.rest.dao.ServiceActions
import com.mocker.rest.dao.mysql.MySqlServiceActions
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.model.{Service, ServiceStats}
import com.mocker.rest.utils.RestMockerUtils._
import com.mocker.rest.utils.ZIOSlick._
import slick.interop.zio.DatabaseProvider
import zio.{IO, URLayer, ZIO, ZLayer}

import scala.concurrent.ExecutionContext

case class RestServiceManager(restMockerDbProvider: DatabaseProvider, serviceActions: ServiceActions) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def createService(service: Service): IO[RestMockerException, Unit] = {
    for {
      _ <- checkServiceNotExists(service)
      _ <- validate(service)
      _ <- serviceActions.upsert(service).asZIO(dbLayer).run
    } yield ()
  }

  def getService(path: String): IO[RestMockerException, Service] = {
    for {
      dbService <- serviceActions.get(path).asZIO(dbLayer).run
      service <- dbService match {
        case Some(service) => ZIO.succeed(service)
        case None          => ZIO.fail(RestMockerException.serviceNotExists(path))
      }
    } yield service
  }

  def updateService(servicePath: String, service: Service): IO[RestMockerException, Unit] = {
    for {
      _ <- if (servicePath != service.path)
        checkServiceNotExists(service)
      else
        ZIO.succeed()
      currentService <- getService(servicePath)
      newService = service.copy(id = currentService.id, creationTime = currentService.creationTime)
      _ <- validate(newService)
      _ <- serviceActions.upsert(newService).asZIO(dbLayer).run
    } yield ()
  }

  def deleteService(path: String): IO[RestMockerException, Unit] = {
    for {
      service <- getService(path)
      _ <- serviceActions.delete(service.id).asZIO(dbLayer).run
    } yield ()
  }

  def getServicesWithStats: IO[RestMockerException, Seq[ServiceStats]] = {
    serviceActions.getWithStats.asZIO(dbLayer).run
  }

  def searchServices(query: String): IO[RestMockerException, Seq[ServiceStats]] = {
    serviceActions.search(query).asZIO(dbLayer).run
  }

  def switchServiceProxy(servicePath: String, isProxyEnabled: Boolean): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      newService = service.copy(isProxyEnabled = isProxyEnabled)
      _ <- validate(newService)
      _ <- serviceActions.upsert(newService).asZIO(dbLayer).run
    } yield ()
  }

  def switchServiceHistory(servicePath: String, isHistoryEnabled: Boolean): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      newService = service.copy(isHistoryEnabled = isHistoryEnabled)
      _ <- serviceActions.upsert(newService).asZIO(dbLayer).run
    } yield ()
  }

  private def validate(service: Service): IO[RestMockerException, Unit] = {
    if (service.isProxyEnabled && service.url.isEmpty)
      ZIO.fail(RestMockerException.proxyUrlMissing(service.path))
    else if (service.path == "service")
      ZIO.fail(RestMockerException.incorrectServicePath(service.path))
    else
      ZIO.succeed()
  }

  private def checkServiceNotExists(service: Service): IO[RestMockerException, Unit] = {
    for {
      dbService <- serviceActions.get(service.path).asZIO(dbLayer).run
      _ <- if (dbService.isDefined)
        ZIO.fail(RestMockerException.serviceAlreadyExists(service.path))
      else
        ZIO.succeed()
    } yield ()
  }
}

object RestServiceManager {

  def layer(implicit ec: ExecutionContext): URLayer[DatabaseProvider, RestServiceManager] = {
    ZLayer.fromZIO {
      for {
        restMockerDatabase <- ZIO.service[DatabaseProvider]
      } yield RestServiceManager(
        restMockerDatabase,
        MySqlServiceActions()
      )
    }
  }
}
