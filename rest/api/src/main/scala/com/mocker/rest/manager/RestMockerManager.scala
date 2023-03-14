package com.mocker.rest.manager

import com.mocker.rest.dao.mysql.{MySqlMockActions, MySqlModelActions, MySqlServiceActions}
import com.mocker.rest.dao.{MockActions, ModelActions, ServiceActions}
import com.mocker.rest.errors.{MockExistsException, ServiceExistsException, ServiceNotExistsException}
import com.mocker.rest.model.{Mock, Model, Service}
import slick.dbio.DBIO
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import slick.jdbc.MySQLProfile.api._
import zio.{ZIO, ZLayer}

import scala.concurrent.ExecutionContext
case class RestMockerManager(
    restMockerDbProvider: DatabaseProvider,
    serviceActions: ServiceActions,
    modelActions: ModelActions,
    mockActions: MockActions
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
      service <- checkServiceExists(servicePath)
      modelId = service.lastModelId + 1
      _ <- ZIO
        .fromDBIO(
          DBIO
            .seq(
              modelActions.upsert(model.copy(id = modelId, serviceId = service.id)),
              serviceActions.upsert(service.copy(lastModelId = modelId))
            )
            .transactionally
        )
        .provide(dbLayer)
    } yield ()
  }

  def createMock(servicePath: String, mock: Mock): ZIO[Any, Throwable, Unit] = {
    for {
      service <- checkServiceExists(servicePath)
      _ <- checkMockNotExists(service, mock)
      mockId = service.lastMockId + 1
      _ <- ZIO
        .fromDBIO(
          DBIO
            .seq(
              mockActions.upsert(mock.copy(id = mockId, serviceId = service.id)),
              serviceActions.upsert(service.copy(lastMockId = mockId))
            )
            .transactionally
        )
        .provide(dbLayer)
    } yield ()
  }

  private def checkServiceExists(path: String) = {
    for {
      dbService <- ZIO.fromDBIO(serviceActions.get(path)).provide(dbLayer)
      service <- dbService match {
        case Some(service) => ZIO.succeed(service)
        case None          => ZIO.fail(ServiceNotExistsException(path))
      }
    } yield service
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

  private def checkMockNotExists(service: Service, mock: Mock) = {
    for {
      dbMock <- ZIO.fromDBIO(mockActions.get(service.id, mock.path)).provide(dbLayer)
      _ <- if (dbMock.isDefined)
        ZIO.fail(MockExistsException(servicePath = service.path, mockPath = mock.path))
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
      } yield RestMockerManager(restMockerDatabase, MySqlServiceActions(), MySqlModelActions(), MySqlMockActions())
    }
  }
}
