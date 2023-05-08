package com.mocker.rest.manager

import com.mocker.rest.dao.ServiceActions
import com.mocker.rest.dao.mysql.MySqlServiceActions
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.model.{Service, ServiceStats}
import com.mocker.rest.utils.Implicits.RedisImplicits._
import com.mocker.rest.utils.RestMockerUtils._
import com.mocker.rest.utils.ZIOSlick._
import slick.interop.zio.DatabaseProvider
import zio.redis.Redis
import zio.{Console, IO, URLayer, ZIO, ZLayer}

import scala.concurrent.ExecutionContext

case class RestServiceManager(
    restMockerDbProvider: DatabaseProvider,
    redisClient: Redis,
    serviceActions: ServiceActions
) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def createService(service: Service): IO[RestMockerException, Unit] = {
    for {
      _ <- checkServiceNotExists(service)
      _ <- validate(service)
      _ <- serviceActions.upsert(service).asZIO(dbLayer).run
      _ <- redisClient
        .set(service.path, service)
        .run
        .catchAll(err => Console.printLineError(err.getMessage).ignoreLogged)
      _ <- setExpirationCacheTime(service)
    } yield ()
  }

  def getService(path: String): IO[RestMockerException, Service] = {
    for {
      cachedService <- redisClient
        .get(path)
        .returning[Service]
        .run
        .catchAll { err =>
          Console.printLineError(err.getMessage).ignoreLogged
          ZIO.succeed(None)
        }
      result <- cachedService match {
        case Some(service) => ZIO.succeed(service)
        case None          => getServiceFromDatabase(path)
      }
    } yield result
  }

  def updateService(servicePath: String, service: Service): IO[RestMockerException, Unit] = {
    for {
      _ <- if (servicePath != service.path)
        checkServiceNotExists(service)
      else
        ZIO.unit
      currentService <- getService(servicePath)
      newService = service.copy(id = currentService.id, creationTime = currentService.creationTime)
      _ <- validate(newService)
      _ <- updateServiceState(servicePath, newService)
    } yield ()
  }

  def deleteService(path: String): IO[RestMockerException, Unit] = {
    for {
      service <- getService(path)
      _ <- redisClient.del(path).run
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
      _ <- updateServiceState(servicePath, newService)
    } yield ()
  }

  def switchServiceHistory(servicePath: String, isHistoryEnabled: Boolean): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      newService = service.copy(isHistoryEnabled = isHistoryEnabled)
      _ <- updateServiceState(servicePath, newService)
    } yield ()
  }

  private def getServiceFromDatabase(path: String): IO[RestMockerException, Service] = {
    for {
      _ <- zio.Console
        .printLine(s"Searching for service $path in database")
        .mapError(e => RestMockerException.internal(e))
      dbService <- serviceActions.get(path).asZIO(dbLayer).run
      service <- dbService match {
        case Some(service) => ZIO.succeed(service)
        case None          => ZIO.fail(RestMockerException.serviceNotExists(path))
      }
    } yield service
  }

  private def updateServiceState(path: String, newService: Service) = {
    for {
      _ <- redisClient.del(path).run
      _ <- redisClient
        .set(newService.path, newService)
        .run
        .catchAll(err => Console.printLineError(err.getMessage).ignoreLogged)
      _ <- setExpirationCacheTime(newService)
      _ <- serviceActions.upsert(newService).asZIO(dbLayer).run
    } yield ()
  }

  private def setExpirationCacheTime(service: Service) = {
    service.expirationTime match {
      case Some(time) =>
        redisClient
          .expireAt(service.path, time)
          .map(_ => ())
          .run
          .catchAll(err => Console.printLineError(err.getMessage).ignoreLogged)
      case None => ZIO.unit
    }
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

  def layer(implicit ec: ExecutionContext): URLayer[DatabaseProvider with Redis, RestServiceManager] = {
    ZLayer.fromZIO {
      for {
        restMockerDatabase <- ZIO.service[DatabaseProvider]
        redisClient <- ZIO.service[Redis]
      } yield RestServiceManager(
        restMockerDatabase,
        redisClient,
        MySqlServiceActions()
      )
    }
  }
}
