package com.mocker.rest.manager

import com.mocker.rest.utils.ZIOSlick._
import com.mocker.rest.dao.mysql.{
  MySqlMockActions,
  MySqlMockHistoryActions,
  MySqlMockResponseActions,
  MySqlModelActions,
  MySqlServiceActions
}
import com.mocker.rest.dao.{MockActions, MockResponseActions, ModelActions, ServiceActions}
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.model._
import com.mocker.rest.utils.HeadersUtils._
import com.mocker.rest.utils.MethodUtils._
import com.mocker.rest.utils.KVPairUtils._
import com.mocker.rest.utils.MockMatchers._
import com.mocker.rest.utils.Orderings._
import com.mocker.rest.utils.PathUtils._
import com.mocker.rest.utils.RestMockerUtils._
import slick.dbio.DBIO
import slick.interop.zio.DatabaseProvider
import zio.http.{Body, Client, Request, Response, URL}
import zio.{IO, URLayer, ZIO, ZLayer}

import scala.concurrent.ExecutionContext
case class RestMockerManager(
    httpClient: Client,
    restMockerDbProvider: DatabaseProvider,
    serviceActions: ServiceActions,
    modelActions: ModelActions,
    mockActions: MockActions,
    mockResponseActions: MockResponseActions,
    mockHistoryActions: MySqlMockHistoryActions
) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def getMockResponse(query: MockQuery): IO[RestMockerException, MockQueryResponse] = {
    for {
      _ <- zio.Console.printLine(query).mapError(e => RestMockerException.internal(e))
      service <- getService(query.servicePath)
      allMocks <- mockActions.getAll(service.id).asZIO(dbLayer).run
      suitableMocks = allMocks.filter(_.matches(query))
      responses <- DBIO
        .sequence(suitableMocks.map(mock => mockResponseActions.getAll(mock.id)))
        .asZIO(dbLayer)
        .run
      suitableResponses = suitableMocks
        .zip(responses)
        .map {
          case (mock, response) => (extractPathParams(query.requestPath, mock), response)
        }
        .map {
          case (pathParams, response) =>
            (
              pathParams,
              response.filter(response => pathParams.sorted == response.pathParams.sorted && response.matches(query))
            )
        }
        .flatMap {
          case (_, response) => response
        }
      _ <- zio.Console.printLine(suitableResponses).mapError(e => RestMockerException.internal(e))
      result <- chooseMockResponse(service, query, suitableMocks, suitableResponses)
    } yield result
  }

  private def chooseMockResponse(
      service: Service,
      query: MockQuery,
      mocks: Seq[Mock],
      responses: Seq[MockResponse]
  ): IO[RestMockerException, MockQueryResponse] = {
    (mocks.toList, responses.toList) match {
      case (_, response :: _) => processStaticResponse(service, query, response)
      case (mock :: _, _)     => processMockTemplate(service, query, mock)
      case (_, _)             => tryGetResponseFromRealService(service, query)
    }
  }

  private def processStaticResponse(
      service: Service,
      query: MockQuery,
      response: MockResponse
  ): IO[RestMockerException, MockQueryResponse] = {
    for {
      // todo: history
      result <- ZIO.succeed(MockQueryResponse.fromMockResponse(response))
    } yield result
  }

  private def processMockTemplate(
      service: Service,
      query: MockQuery,
      mock: Mock
  ): IO[RestMockerException, MockQueryResponse] = {
    for {
      // todo: history
      modelOpt <- mock.responseModelId match {
        case Some(id) => modelActions.get(id).asZIO(dbLayer).run
        case None     => ZIO.succeed(None)
      }
      response = modelOpt.map(model => MockQueryResponse.fromModel(model)).getOrElse(MockQueryResponse.default)
      result <- ZIO.succeed(response)
    } yield result
  }

  private def tryGetResponseFromRealService(
      service: Service,
      mockQuery: MockQuery
  ): IO[RestMockerException, MockQueryResponse] = {
    // todo: history
    if (service.isProxyEnabled) {
      for {
        request <- buildHttpRequest(service, mockQuery)
        response <- httpClient.request(request).mapError(RestMockerException.cantGetProxiedResponse)
        mockQueryResponse <- buildMockResponse(response)
      } yield mockQueryResponse
    } else
      ZIO.fail(RestMockerException.suitableMockNotFound)
  }

  private def buildHttpRequest(service: Service, mockQuery: MockQuery): IO[RestMockerException, Request] = {
    for {
      serviceUrl <- service.url match {
        case Some(url) => ZIO.succeed(url)
        case None      => ZIO.fail(RestMockerException.proxyUrlMissing(service.path))
      }
      fullPath = serviceUrl + {
        if (serviceUrl.endsWith("/")) mockQuery.requestPath.drop(1)
        else mockQuery.requestPath
      }
      url <- ZIO
        .fromEither(URL.fromString(fullPath).map(_.setQueryParams(mockQuery.queryParams.toQueryParams)))
        .mapError(RestMockerException.cantGetProxiedResponse)
      proxiedRequest <- ZIO.succeed(
        Request
          .default(
            body = mockQuery.body.map(Body.fromString(_)).getOrElse(Body.empty),
            url = url,
            method = mockQuery.method.toZIOMethod
          )
          .setHeaders(mockQuery.headers.toHttpHeaders)
      )
    } yield proxiedRequest
  }

  private def buildMockResponse(response: Response): IO[RestMockerException, MockQueryResponse] = {
    for {
      responseContent <- response.body.asString.mapError(RestMockerException.cantGetProxiedResponse)
    } yield MockQueryResponse(
      response.status.code,
      response.headers.toKVPairs,
      responseContent
    )
  }

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

  def upsertModel(servicePath: String, model: Model): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      _ <- modelActions.upsert(model.copy(serviceId = service.id)).asZIO(dbLayer).run
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
        modelActions.delete(service.id, modelId).asZIO(dbLayer).run
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

  def createMockResponse(
      servicePath: String,
      mockId: Long,
      mockResponse: MockResponse
  ): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      mock <- checkMockExists(service, mockId)
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

  def updateMockStaticResponse(
      servicePath: String,
      mockId: Long,
      responseId: Long,
      mockResponse: MockResponse
  ): IO[RestMockerException, Unit] = {
    for {
      mock <- getMock(servicePath, mockId)
      _ <- if (isMockResponseValid(mock, mockResponse))
        mockResponseActions.upsert(mockResponse.copy(id = responseId)).asZIO(dbLayer).run
      else
        ZIO.fail(RestMockerException.invalidMockResponse(mock.path, mockResponse.name))
    } yield ()
  }

  def deleteMockStaticResponse(servicePath: String, mockId: Long, responseId: Long): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      mock <- checkMockExists(service, mockId)
      _ <- mockResponseActions.delete(mock.id, responseId).asZIO(dbLayer).run
    } yield ()
  }

  def deleteAllMockStaticResponses(servicePath: String, mockId: Long): IO[RestMockerException, Unit] = {
    for {
      service <- getService(servicePath)
      mock <- checkMockExists(service, mockId)
      _ <- mockResponseActions.deleteAll(mock.id).asZIO(dbLayer).run
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

  def layer(implicit ec: ExecutionContext): URLayer[DatabaseProvider with Client, RestMockerManager] = {
    ZLayer.fromZIO {
      for {
        httpClient <- ZIO.service[Client]
        restMockerDatabase <- ZIO.service[DatabaseProvider]
      } yield RestMockerManager(
        httpClient,
        restMockerDatabase,
        MySqlServiceActions(),
        MySqlModelActions(),
        MySqlMockActions(),
        MySqlMockResponseActions(),
        MySqlMockHistoryActions()
      )
    }
  }
}
