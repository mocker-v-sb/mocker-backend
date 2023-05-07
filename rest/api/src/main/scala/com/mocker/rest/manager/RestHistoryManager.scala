package com.mocker.rest.manager

import com.mocker.rest.dao.MockHistoryActions
import com.mocker.rest.dao.mysql.MySqlMockHistoryActions
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.mock_history.ResponseSourceNamespace.ResponseSource
import com.mocker.rest.mock_history.ResponseTimeSort
import com.mocker.rest.model.MockHistoryItem
import com.mocker.rest.request.Method
import com.mocker.rest.utils.ZIOSlick._
import slick.interop.zio.DatabaseProvider
import zio.{IO, URLayer, ZIO, ZLayer}

import java.sql.Timestamp
import scala.concurrent.ExecutionContext

case class RestHistoryManager(restMockerDbProvider: DatabaseProvider, mockHistoryActions: MockHistoryActions) {

  private val dbLayer = ZLayer.succeed(restMockerDbProvider)

  def getServiceHistory(
      serviceId: Long,
      searchUrl: Option[String],
      from: Option[Timestamp],
      to: Option[Timestamp],
      statusCodes: Set[Int],
      responseSources: Set[ResponseSource],
      methods: Set[Method],
      sort: ResponseTimeSort,
      limit: Int,
      shift: Int
  ): IO[RestMockerException, Seq[MockHistoryItem]] = {
    mockHistoryActions
      .search(serviceId, searchUrl, from, to, statusCodes, responseSources, methods, sort, limit, shift)
      .asZIO(dbLayer)
      .mapError(RestMockerException.internal)
  }

  def countHistoryItems(
      serviceId: Long,
      searchUrl: Option[String],
      from: Option[Timestamp],
      to: Option[Timestamp],
      statusCodes: Set[Int],
      responseSources: Set[ResponseSource],
      methods: Set[Method]
  ): IO[RestMockerException, Int] = {
    mockHistoryActions
      .count(serviceId, searchUrl, from, to, statusCodes, responseSources, methods)
      .asZIO(dbLayer)
      .mapError(RestMockerException.internal)
  }
}

object RestHistoryManager {

  def layer(implicit ec: ExecutionContext): URLayer[DatabaseProvider, RestHistoryManager] = {
    ZLayer.fromZIO {
      for {
        restMockerDatabase <- ZIO.service[DatabaseProvider]
      } yield RestHistoryManager(
        restMockerDatabase,
        MySqlMockHistoryActions()
      )
    }
  }
}
