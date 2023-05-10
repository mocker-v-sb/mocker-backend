package com.mocker.rest.manager

import com.mocker.rest.dao.ServiceActions
import com.mocker.rest.dao.mysql.MySqlServiceActions
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.manager.RestServiceManager.getRedisKey
import com.mocker.rest.model.{Service, ServiceStats}
import com.mocker.rest.utils.Implicits.RedisImplicits._
import com.mocker.rest.utils.RestMockerUtils._
import com.mocker.rest.utils.ZIOSlick._
import slick.interop.zio.DatabaseProvider
import zio.redis.Redis
import zio.{Console, IO, URLayer, ZIO, ZLayer}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

case class RestServiceManager(
    restMockerDbProvider: DatabaseProvider,
    redisClient: Redis,
    serviceActions: ServiceActions
) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def createService(user: String, service: Service): IO[RestMockerException, Unit] = {
    for {
      _ <- checkServiceNotExists(service)
      _ <- validate(service)
      _ <- serviceActions.upsert(service.copy(owner = user)).asZIO(dbLayer).run
    } yield ()
  }

  def getService(user: String, serviceId: Long): IO[RestMockerException, Service] = {
    for {
      dbService <- serviceActions.get(serviceId).asZIO(dbLayer).run
      service <- dbService match {
        case Some(service) => ZIO.succeed(service)
        case None          => ZIO.fail(RestMockerException.serviceNotExists(serviceId))
      }
      _ <- checkServiceOwner(user, service)
    } yield service
  }

  def getService(user: String, path: String, checkAuth: Boolean = true): IO[RestMockerException, Service] = {
    for {
      cachedService <- redisClient
        .get(getRedisKey(path))
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
      _ <- if (checkAuth)
        checkServiceOwner(user, result)
      else
        ZIO.succeed()
    } yield result
  }

  def updateService(user: String, servicePath: String, service: Service): IO[RestMockerException, Unit] = {
    for {
      _ <- if (servicePath != service.path)
        checkServiceNotExists(service)
      else
        ZIO.unit
      currentService <- getService(user, servicePath)
      newService = service.copy(
        id = currentService.id,
        creationTime = currentService.creationTime,
        owner = currentService.owner
      )
      _ <- validate(newService)
      _ <- updateServiceState(servicePath, newService)
    } yield ()
  }

  def deleteService(user: String, path: String): IO[RestMockerException, Unit] = {
    for {
      service <- getService(user, path)
      _ <- redisClient.del(getRedisKey(path)).run
      _ <- serviceActions.delete(service.id).asZIO(dbLayer).run
    } yield ()
  }

  def getServicesWithStats(user: String): IO[RestMockerException, Seq[ServiceStats]] = {
    serviceActions.getWithStats(user).asZIO(dbLayer).run
  }

  def searchServices(user: String, query: String): IO[RestMockerException, Seq[ServiceStats]] = {
    serviceActions.search(user, query).asZIO(dbLayer).run
  }

  def switchServiceProxy(user: String, servicePath: String, isProxyEnabled: Boolean): IO[RestMockerException, Unit] = {
    for {
      service <- getService(user, servicePath)
      newService = service.copy(isProxyEnabled = isProxyEnabled)
      _ <- validate(newService)
      _ <- updateServiceState(servicePath, newService)
    } yield ()
  }

  def switchServiceHistory(
      user: String,
      servicePath: String,
      isHistoryEnabled: Boolean
  ): IO[RestMockerException, Unit] = {
    for {
      service <- getService(user, servicePath)
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
        case Some(service) =>
          for {
            _ <- redisClient
              .set(getRedisKey(service.path), service, expireTime = Some(zio.Duration.fromScala(3.minutes)))
              .run
              .catchAll(err => Console.printLineError(err.getMessage).ignoreLogged)
            result <- ZIO.succeed(service)
          } yield result
        case None => ZIO.fail(RestMockerException.serviceNotExists(path))
      }
    } yield service
  }

  private def checkServiceOwner(user: String, service: Service): IO[RestMockerException, Unit] = {
    if (service.owner != user) {
      ZIO.fail(RestMockerException.accessDenied(user, service))
    } else {
      ZIO.succeed()
    }
  }

  private def updateServiceState(path: String, newService: Service) = {
    for {
      _ <- redisClient.del(getRedisKey(path)).run
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

  def getRedisKey(path: String) = s"service:$path"

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
