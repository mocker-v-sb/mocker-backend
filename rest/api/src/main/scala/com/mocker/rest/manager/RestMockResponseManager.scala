package com.mocker.rest.manager

import com.mocker.rest.dao.MockResponseActions
import com.mocker.rest.dao.mysql.MySqlMockResponseActions
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.model.{Mock, MockResponse}
import com.mocker.rest.utils.RestMockerUtils._
import com.mocker.rest.utils.ZIOSlick._
import slick.dbio.DBIO
import slick.interop.zio.DatabaseProvider
import zio.{IO, URLayer, ZIO, ZLayer}

import scala.concurrent.ExecutionContext

case class RestMockResponseManager(
    restMockerDbProvider: DatabaseProvider,
    serviceManager: RestServiceManager,
    mockManager: RestMockManager,
    mockResponseActions: MockResponseActions
) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def createMockResponse(
      servicePath: String,
      mockId: Long,
      mockResponse: MockResponse
  ): IO[RestMockerException, Unit] = {
    for {
      service <- serviceManager.getService(servicePath)
      mock <- mockManager.checkMockExists(service, mockId)
      _ <- if (isMockResponseValid(mock, mockResponse))
        mockResponseActions.upsert(mockResponse).asZIO(dbLayer).run
      else
        ZIO.fail(RestMockerException.invalidMockResponse(mock.path, mockResponse.name))
    } yield ()
  }

  def getMockResponse(servicePath: String, mockId: Long, responseId: Long): IO[RestMockerException, MockResponse] = {
    for {
      service <- serviceManager.getService(servicePath)
      mock <- mockManager.checkMockExists(service, mockId)
      mockResponse <- getMockResponse(mock.id, responseId)
    } yield mockResponse
  }

  def getAllMockResponses(servicePath: String, mockId: Long): IO[RestMockerException, (Mock, Seq[MockResponse])] = {
    for {
      service <- serviceManager.getService(servicePath)
      mock <- mockManager.checkMockExists(service, mockId)
      mockResponses <- mockResponseActions.getAll(mock.id).asZIO(dbLayer).run
    } yield (mock, mockResponses)
  }

  def getAllMocksResponses(mockIds: Seq[Long]): IO[RestMockerException, Seq[Seq[MockResponse]]] = {
    DBIO
      .sequence(mockIds.map(id => mockResponseActions.getAll(id)))
      .asZIO(dbLayer)
      .run
  }

  def updateMockStaticResponse(
      servicePath: String,
      mockId: Long,
      responseId: Long,
      mockResponse: MockResponse
  ): IO[RestMockerException, Unit] = {
    for {
      mock <- mockManager.getMock(servicePath, mockId)
      _ <- if (isMockResponseValid(mock, mockResponse))
        mockResponseActions.upsert(mockResponse.copy(id = responseId)).asZIO(dbLayer).run
      else
        ZIO.fail(RestMockerException.invalidMockResponse(mock.path, mockResponse.name))
    } yield ()
  }

  def deleteMockStaticResponse(servicePath: String, mockId: Long, responseId: Long): IO[RestMockerException, Unit] = {
    for {
      service <- serviceManager.getService(servicePath)
      mock <- mockManager.checkMockExists(service, mockId)
      _ <- mockResponseActions.delete(mock.id, responseId).asZIO(dbLayer).run
    } yield ()
  }

  def deleteAllMockStaticResponses(servicePath: String, mockId: Long): IO[RestMockerException, Unit] = {
    for {
      service <- serviceManager.getService(servicePath)
      mock <- mockManager.checkMockExists(service, mockId)
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

  private def isMockResponseValid(mock: Mock, mockResponse: MockResponse): Boolean = {
    mockResponse.requestHeaders.map(_.name).forall(mock.requestHeaders.contains) &&
    mockResponse.responseHeaders.map(_.name).forall(mock.responseHeaders.contains) &&
    mockResponse.queryParams.map(_.name).forall(mock.queryParams.contains) &&
    mockResponse.pathParams.map(_.name).forall(mock.pathParams.contains)
  }
}

object RestMockResponseManager {

  def layer(
      implicit ec: ExecutionContext
  ): URLayer[DatabaseProvider with RestServiceManager with RestMockManager, RestMockResponseManager] = {
    ZLayer.fromZIO {
      for {
        restMockerDatabase <- ZIO.service[DatabaseProvider]
        serviceManager <- ZIO.service[RestServiceManager]
        mockManager <- ZIO.service[RestMockManager]
      } yield RestMockResponseManager(
        restMockerDatabase,
        serviceManager,
        mockManager,
        MySqlMockResponseActions()
      )
    }
  }
}
