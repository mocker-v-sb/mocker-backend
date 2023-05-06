package com.mocker.rest.manager

import com.mocker.rest.dao.MockActions
import com.mocker.rest.dao.mysql.MySqlMockActions
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.model.{Mock, MockPatch, Service}
import com.mocker.rest.utils.RestMockerUtils._
import com.mocker.rest.utils.ZIOSlick._
import slick.dbio.DBIO
import slick.interop.zio.DatabaseProvider
import zio.{IO, URLayer, ZIO, ZLayer}

import scala.concurrent.ExecutionContext

case class RestMockManager(
    restMockerDbProvider: DatabaseProvider,
    serviceManager: RestServiceManager,
    mockActions: MockActions
) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def createMock(servicePath: String, mock: Mock): IO[RestMockerException, Unit] = {
    for {
      service <- serviceManager.getService(servicePath)
      _ <- checkMockNotExists(service, mock)
      _ <- mockActions.upsert(mock.copy(serviceId = service.id)).asZIO(dbLayer).run
    } yield ()
  }

  def getMock(servicePath: String, mockId: Long): IO[RestMockerException, Mock] = {
    for {
      service <- serviceManager.getService(servicePath)
      mock <- checkMockExists(service, mockId)
    } yield mock
  }

  def getAll(serviceId: Long): IO[RestMockerException, Seq[Mock]] = {
    mockActions.getAll(serviceId).asZIO(dbLayer).run
  }

  def getAllServiceMocks(servicePath: String): IO[RestMockerException, Seq[Mock]] = {
    for {
      service <- serviceManager.getService(servicePath)
      mocks <- mockActions.getAll(service.id).asZIO(dbLayer).run
    } yield mocks
  }

  def findByModel(serviceId: Long, modelId: Long): IO[RestMockerException, Seq[Mock]] = {
    mockActions.findByModel(serviceId, modelId).asZIO(dbLayer).run
  }

  def findByModels(serviceId: Long, modelIds: Seq[Long]): IO[RestMockerException, Seq[Mock]] = {
    DBIO
      .sequence(modelIds.map(modelId => mockActions.findByModel(serviceId, modelId)))
      .asZIO(dbLayer)
      .run
      .map(_.flatten)
  }

  def updateMock(servicePath: String, mockId: Long, patch: MockPatch): IO[RestMockerException, Unit] = {
    for {
      currentMock <- getMock(servicePath, mockId)
      _ <- mockActions
        .upsert(
          currentMock.copy(
            name = patch.name,
            description = patch.description,
            method = patch.method,
            requestModelId = patch.requestModelId,
            responseModelId = patch.responseModelId
          )
        )
        .asZIO(dbLayer)
        .run
    } yield ()
  }

  def deleteMock(servicePath: String, mockId: Long): IO[RestMockerException, Unit] = {
    for {
      service <- serviceManager.getService(servicePath)
      _ <- mockActions.delete(service.id, mockId).asZIO(dbLayer).run
    } yield ()
  }

  def deleteAllMocks(servicePath: String): IO[RestMockerException, Unit] = {
    for {
      service <- serviceManager.getService(servicePath)
      _ <- mockActions.deleteAll(service.id).asZIO(dbLayer).run
    } yield ()
  }

  def checkMockNotExists(service: Service, mock: Mock): IO[RestMockerException, Unit] = {
    for {
      dbMock <- mockActions.get(service.id, mock.path).asZIO(dbLayer).run
      _ <- if (dbMock.isDefined)
        ZIO.fail(RestMockerException.mockAlreadyExists(servicePath = service.path, mockPath = mock.path))
      else
        ZIO.succeed()
    } yield ()
  }

  def checkMockExists(service: Service, mockId: Long): IO[RestMockerException, Mock] = {
    for {
      dbMock <- mockActions.get(serviceId = service.id, mockId = mockId).asZIO(dbLayer).run
      result <- dbMock match {
        case Some(mock) => ZIO.succeed(mock)
        case None       => ZIO.fail(RestMockerException.mockNotExists(servicePath = service.path, mockId = mockId))
      }
    } yield result
  }
}

object RestMockManager {

  def layer(implicit ec: ExecutionContext): URLayer[DatabaseProvider with RestServiceManager, RestMockManager] = {
    ZLayer.fromZIO {
      for {
        restMockerDatabase <- ZIO.service[DatabaseProvider]
        serviceManager <- ZIO.service[RestServiceManager]
      } yield RestMockManager(
        restMockerDatabase,
        serviceManager,
        MySqlMockActions()
      )
    }
  }
}
