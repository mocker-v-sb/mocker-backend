package com.mocker.rest.manager

import com.mocker.common.utils.ZIOSlick._
import com.mocker.rest.dao.mysql.{MySqlMockActions, MySqlMockResponseActions, MySqlModelActions, MySqlServiceActions}
import com.mocker.rest.dao.{MockActions, MockResponseActions, ModelActions, ServiceActions}
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.model._
import com.mocker.rest.utils.RestMockerUtils._
import slick.dbio.DBIO
import slick.interop.zio.DatabaseProvider
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

  def getServicesWithStats: IO[RestMockerException, Seq[ServiceStats]] = {
    serviceActions.getWithStats.asZIO(dbLayer).run
  }

  def searchServices(query: String): IO[RestMockerException, Seq[ServiceStats]] = {
    serviceActions.search(query).asZIO(dbLayer).run
  }

  def deleteService(path: String): IO[RestMockerException, Unit] = {
    for {
      service <- getService(path)
      _ <- serviceActions.delete(service.id).asZIO(dbLayer).run
    } yield ()
  }

  def createModel(servicePath: String, model: Model): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      _ <- modelActions
        .upsert(model.copy(serviceId = service.id))
        .asZIO(dbLayer)
        .run
    } yield ()
  }

  def getModel(servicePath: String, modelId: Long): IO[RestMockerException, Model] = {
    for {
      service <- getService(servicePath)
      model <- checkModelExists(service, modelId)
    } yield model
  }

  def getAllServiceModels(servicePath: String): IO[RestMockerException, Seq[Model]] = {
    for {
      service <- getService(servicePath)
      models <- modelActions.getAll(service.id).asZIO(dbLayer).run
    } yield models
  }

  def deleteModel(servicePath: String, modelId: Long): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      existingMocks <- mockActions.findByModel(service.id, modelId).asZIO(dbLayer).run
      _ <- if (existingMocks.nonEmpty)
        ZIO.fail(RestMockerException.modelInUse(servicePath, existingMocks))
      else
        ZIO.succeed(modelActions.delete(service.id, modelId).asZIO(dbLayer).run)
    } yield ()
  }

  def deleteAllModels(servicePath: String): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      models <- modelActions.getAll(service.id).asZIO(dbLayer).run
      mocks <- DBIO
        .sequence(models.map(model => mockActions.findByModel(service.id, model.id)))
        .asZIO(dbLayer)
        .run
        .map(_.flatten)
      _ <- mocks.toList match {
        case Nil => modelActions.deleteAll(service.id).asZIO(dbLayer).run
        case _   => ZIO.fail(RestMockerException.modelInUse(servicePath, mocks))
      }
    } yield ()
  }

  def createMock(servicePath: String, mock: Mock): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      _ <- checkMockNotExists(service, mock)
      _ <- mockActions.upsert(mock.copy(serviceId = service.id)).asZIO(dbLayer).run
    } yield ()
  }

  def getMock(servicePath: String, mockId: Long): IO[RestMockerException, Mock] = {
    for {
      service <- getService(servicePath)
      mock <- checkMockExists(service, mockId)
    } yield mock
  }

  def getAllServiceMocks(servicePath: String): IO[RestMockerException, Seq[Mock]] = {
    for {
      service <- getService(servicePath)
      mocks <- mockActions.getAll(service.id).asZIO(dbLayer).run
    } yield mocks
  }

  def deleteMock(servicePath: String, mockId: Long): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      _ <- mockActions.delete(service.id, mockId).asZIO(dbLayer).run
    } yield ()
  }

  def deleteAllMocks(servicePath: String): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      _ <- mockActions.deleteAll(service.id).asZIO(dbLayer).run
    } yield ()
  }

  def createMockResponse(mockId: Long, mockResponse: MockResponse): IO[RestMockerException, Unit] = {
    for {
      mock <- checkMockExists(mockId)
      _ <- if (isMockResponseValid(mock, mockResponse))
        mockResponseActions.upsert(mockResponse).asZIO(dbLayer).run
      else
        ZIO.fail(RestMockerException.invalidMockResponse(mock.path, mockResponse.name))
    } yield ()
  }

  def getMockResponse(servicePath: String, mockId: Long, responseId: Long): IO[RestMockerException, MockResponse] = {
    for {
      service <- getService(servicePath)
      mock <- checkMockExists(service, mockId)
      mockResponse <- getMockResponse(mock.id, responseId)
    } yield mockResponse
  }

  def getAllMockResponses(servicePath: String, mockId: Long): IO[RestMockerException, (Mock, Seq[MockResponse])] = {
    for {
      service <- getService(servicePath)
      mock <- checkMockExists(service, mockId)
      mockResponses <- mockResponseActions.getAll(mock.id).asZIO(dbLayer).run
    } yield (mock, mockResponses)
  }

  def deleteMockStaticResponse(servicePath: String, mockId: Long, responseId: Long): IO[RestMockerException, Unit] = {
    for {
      _ <- getService(servicePath)
      _ <- mockResponseActions.delete(mockId, responseId).asZIO(dbLayer).run
    } yield ()
  }

  def deleteAllMockStaticResponses(servicePath: String, mockId: Long): IO[RestMockerException, Unit] = {
    for {
      _ <- getService(servicePath)
      _ <- mockResponseActions.deleteAll(mockId).asZIO(dbLayer).run
    } yield ()
  }

  private def getMockResponse(mockId: Long, responseId: Long): IO[RestMockerException, MockResponse] = {
    for {
      dbResponse <- mockResponseActions
        .get(mockId = mockId, responseId = responseId)
        .asZIO(dbLayer)
        .run
      result <- dbResponse match {
        case Some(response) => ZIO.succeed(response)
        case None           => ZIO.fail(RestMockerException.responseNotExists(mockId = mockId, responseId = responseId))
      }
    } yield result
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

  private def checkModelExists(service: Service, modelId: Long): IO[RestMockerException, Model] = {
    for {
      dbModel <- modelActions.get(serviceId = service.id, modelId = modelId).asZIO(dbLayer).run
      result <- dbModel match {
        case Some(model) => ZIO.succeed(model)
        case None        => ZIO.fail(RestMockerException.modelNotExists(servicePath = service.path, modelId = modelId))
      }
    } yield result
  }

  private def checkMockNotExists(service: Service, mock: Mock): IO[RestMockerException, Unit] = {
    for {
      dbMock <- mockActions.get(service.id, mock.path).asZIO(dbLayer).run
      _ <- if (dbMock.isDefined)
        ZIO.fail(RestMockerException.mockAlreadyExists(servicePath = service.path, mockPath = mock.path))
      else
        ZIO.succeed()
    } yield ()
  }

  private def checkMockExists(mockId: Long): IO[RestMockerException, Mock] = {
    for {
      dbMock <- mockActions.get(mockId).asZIO(dbLayer).run
      mock <- dbMock match {
        case Some(mock) => ZIO.succeed(mock)
        case None       => ZIO.fail(RestMockerException.mockNotExists(mockId))
      }
    } yield mock
  }

  private def checkMockExists(service: Service, mockId: Long): IO[RestMockerException, Mock] = {
    for {
      dbMock <- mockActions.get(serviceId = service.id, mockId = mockId).asZIO(dbLayer).run
      result <- dbMock match {
        case Some(mock) => ZIO.succeed(mock)
        case None       => ZIO.fail(RestMockerException.mockNotExists(servicePath = service.path, mockId = mockId))
      }
    } yield result
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
