package com.mocker.rest.manager

import com.mocker.common.utils.ZIOSlick._
import com.mocker.rest.dao.mysql.{MySqlMockActions, MySqlMockResponseActions, MySqlModelActions, MySqlServiceActions}
import com.mocker.rest.dao.{MockActions, MockResponseActions, ModelActions, ServiceActions}
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.model.{Mock, MockResponse, Model, Service}
import slick.dbio.DBIO
import slick.interop.zio.DatabaseProvider
import slick.jdbc.MySQLProfile.api._
import zio.{IO, URLayer, ZIO, ZLayer}

import scala.concurrent.ExecutionContext
case class RestMockerManager(
    restMockerDbProvider: DatabaseProvider,
    serviceActions: ServiceActions,
    modelActions: ModelActions,
    mockActions: MockActions,
    mockResponseActions: MockResponseActions
) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def createService(service: Service): IO[RestMockerException, Unit] = {
    for {
      _ <- checkServiceNotExists(service)
      _ <- serviceActions.upsert(service).asZIO(dbLayer).mapError(RestMockerException.internal)
    } yield ()
  }

  def createModel(servicePath: String, model: Model): IO[RestMockerException, Unit] = {
    for {
      service <- checkServiceExists(servicePath)
      _ <- modelActions
        .upsert(model.copy(serviceId = service.id))
        .asZIO(dbLayer)
        .mapError(RestMockerException.internal)
    } yield ()
  }

  def createMock(servicePath: String, mock: Mock): IO[RestMockerException, Unit] = {
    for {
      service <- checkServiceExists(servicePath)
      _ <- checkMockNotExists(service, mock)
      _ <- mockActions.upsert(mock.copy(serviceId = service.id)).asZIO(dbLayer).mapError(RestMockerException.internal)
    } yield ()
  }

  def createMockResponse(mockId: Long, mockResponse: MockResponse): IO[RestMockerException, Unit] = {
    for {
      mock <- checkMockExists(mockId)
      _ <- if (isMockResponseValid(mock, mockResponse))
        mockResponseActions.upsert(mockResponse).asZIO(dbLayer).mapError(RestMockerException.internal)
      else
        ZIO.fail(RestMockerException.invalidMockResponse(mock.path, mockResponse.name))
    } yield ()
  }

  private def checkServiceExists(path: String): IO[RestMockerException, Service] = {
    for {
      dbService <- serviceActions.get(path).asZIO(dbLayer).mapError(RestMockerException.internal)
      service <- dbService match {
        case Some(service) => ZIO.succeed(service)
        case None          => ZIO.fail(RestMockerException.serviceNotExists(path))
      }
    } yield service
  }

  private def checkServiceNotExists(service: Service): IO[RestMockerException, Unit] = {
    for {
      dbService <- serviceActions.get(service.path).asZIO(dbLayer).mapError(RestMockerException.internal)
      _ <- if (dbService.isDefined)
        ZIO.fail(RestMockerException.serviceAlreadyExists(service.path))
      else
        ZIO.succeed()
    } yield ()
  }

  private def checkMockNotExists(service: Service, mock: Mock): IO[RestMockerException, Unit] = {
    for {
      dbMock <- mockActions.get(service.id, mock.path).asZIO(dbLayer).mapError(RestMockerException.internal)
      _ <- if (dbMock.isDefined)
        ZIO.fail(RestMockerException.mockAlreadyExists(servicePath = service.path, mockPath = mock.path))
      else
        ZIO.succeed()
    } yield ()
  }

  private def checkMockExists(mockId: Long): IO[RestMockerException, Mock] = {
    for {
      dbMock <- mockActions.get(mockId).asZIO(dbLayer).mapError(RestMockerException.internal)
      mock <- dbMock match {
        case Some(mock) => ZIO.succeed(mock)
        case None       => ZIO.fail(RestMockerException.mockNotExists(mockId))
      }
    } yield mock
  }

  private def isMockResponseValid(mock: Mock, mockResponse: MockResponse): Boolean = {
    mockResponse.requestHeaders.map(_.name).forall(mock.requestHeaders.contains) &&
    mockResponse.responseHeaders.map(_.name).forall(mock.responseHeaders.contains) &&
    mockResponse.queryParams.map(_.name).forall(mock.queryParams.contains) &&
    mockResponse.pathParams.map(_.name).forall(mock.pathParams.contains)
  }
}

object RestMockerManager {

  def layer(implicit ec: ExecutionContext): URLayer[DatabaseProvider, RestMockerManager] = {
    ZLayer.fromZIO {
      for {
        restMockerDatabase <- ZIO.service[DatabaseProvider]
      } yield RestMockerManager(
        restMockerDatabase,
        MySqlServiceActions(),
        MySqlModelActions(),
        MySqlMockActions(),
        MySqlMockResponseActions()
      )
    }
  }
}
