package com.mocker.rest.manager

import com.mocker.rest.dao.mysql.MySqlMockHistoryActions
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.mock_history.ResponseSourceNamespace.ResponseSource
import com.mocker.rest.model._
import com.mocker.rest.utils.HeadersUtils._
import com.mocker.rest.utils.KVPairUtils._
import com.mocker.rest.utils.MethodUtils._
import com.mocker.rest.utils.MockMatchers._
import com.mocker.rest.utils.Orderings._
import com.mocker.rest.utils.PathUtils._
import com.mocker.rest.utils.ZIOSlick._
import slick.interop.zio.DatabaseProvider
import zio.http._
import zio.redis.Redis
import zio.{Console, IO, URLayer, ZIO, ZLayer}

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.ExecutionContext

case class RestResponseManager(
    httpClient: Client,
    restMockerDbProvider: DatabaseProvider,
    redisClient: Redis,
    serviceManager: RestServiceManager,
    modelManager: RestModelManager,
    mockManager: RestMockManager,
    mockResponseManager: RestMockResponseManager,
    mockHistoryActions: MySqlMockHistoryActions
) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def getMockResponse(query: MockQuery): IO[RestMockerException, MockQueryResponse] = {
    for {
      _ <- zio.Console.printLine(query).mapError(e => RestMockerException.internal(e))
      service <- serviceManager.getService(query.servicePath)
      allMocks <- mockManager.getAll(service.id)
      suitableMocks = allMocks.filter(_.matches(query))
      responses <- mockResponseManager.getAllMocksResponses(suitableMocks.map(_.id))
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
      staticResponse: MockResponse
  ): IO[RestMockerException, MockQueryResponse] = {
    val queryResponse = MockQueryResponse.fromMockResponse(staticResponse)
    for {
      result <- ZIO.succeed(queryResponse)
      _ <- if (service.isHistoryEnabled) {
        mockHistoryActions
          .insert(
            prepareHistoryItem(service, query, queryResponse).copy(responseSource = ResponseSource.STATIC_RESPONSE)
          )
          .asZIO(dbLayer)
          .catchAll(err => Console.printLineError(err.getMessage).ignoreLogged)
      } else {
        ZIO.unit
      }
    } yield result
  }

  private def processMockTemplate(
      service: Service,
      query: MockQuery,
      mock: Mock
  ): IO[RestMockerException, MockQueryResponse] = {
    for {
      modelOpt <- mock.responseModelId match {
        case Some(id) => modelManager.getModel(id)
        case None     => ZIO.succeed(None)
      }
      response = modelOpt.map(model => MockQueryResponse.fromModel(model)).getOrElse(MockQueryResponse.default)
      result <- ZIO.succeed(response)
      _ <- if (service.isHistoryEnabled) {
        mockHistoryActions
          .insert(prepareHistoryItem(service, query, response).copy(responseSource = ResponseSource.MOCK_TEMPLATE))
          .asZIO(dbLayer)
          .catchAll(err => Console.printLineError(err.getMessage).ignoreLogged)
      } else {
        ZIO.unit
      }
    } yield result
  }

  private def tryGetResponseFromRealService(
      service: Service,
      query: MockQuery
  ): IO[RestMockerException, MockQueryResponse] = {
    if (service.isProxyEnabled) {
      for {
        request <- buildHttpRequest(service, query)
        response <- httpClient.request(request).mapError(RestMockerException.cantGetProxiedResponse)
        mockQueryResponse <- buildMockResponse(response)
        _ <- if (service.isHistoryEnabled) {
          mockHistoryActions
            .insert(
              prepareHistoryItem(service, query, mockQueryResponse)
                .copy(responseUrl = request.url.encode, responseSource = ResponseSource.PROXIED_RESPONSE)
            )
            .asZIO(dbLayer)
            .catchAll(err => Console.printLineError(err.getMessage).ignoreLogged)
        } else {
          ZIO.unit
        }
      } yield mockQueryResponse
    } else
      ZIO.fail(RestMockerException.suitableMockNotFound)
  }

  private def prepareHistoryItem(service: Service, query: MockQuery, response: MockQueryResponse): MockHistoryItem = {
    MockHistoryItem(
      serviceId = service.id,
      method = query.method,
      queryUrl = query.rawUrl,
      responseUrl = query.rawUrl,
      responseSource = ResponseSource.EMPTY,
      statusCode = response.statusCode,
      responseHeaders = response.headers,
      requestHeaders = query.headers,
      responseTime = Instant.now(),
      response = response.content
    )
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
}

object RestResponseManager {

  def layer(
      implicit ec: ExecutionContext
  ): URLayer[DatabaseProvider with Client with Redis with RestServiceManager with RestMockManager with RestModelManager with RestMockResponseManager, RestResponseManager] = {
    ZLayer.fromZIO {
      for {
        httpClient <- ZIO.service[Client]
        restMockerDatabase <- ZIO.service[DatabaseProvider]
        redisClient <- ZIO.service[Redis]
        serviceManager <- ZIO.service[RestServiceManager]
        modelManager <- ZIO.service[RestModelManager]
        mockManager <- ZIO.service[RestMockManager]
        mockResponseManager <- ZIO.service[RestMockResponseManager]
      } yield RestResponseManager(
        httpClient,
        restMockerDatabase,
        redisClient,
        serviceManager,
        modelManager,
        mockManager,
        mockResponseManager,
        MySqlMockHistoryActions()
      )
    }
  }
}
