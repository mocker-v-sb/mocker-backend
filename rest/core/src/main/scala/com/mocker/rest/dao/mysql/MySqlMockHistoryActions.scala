package com.mocker.rest.dao.mysql

import com.mocker.rest.dao.MockHistoryActions
import com.mocker.rest.dao.implicits.MySqlImplicits._
import com.mocker.rest.dao.mysql.MySqlMockHistoryActions.MockHistoryTable
import com.mocker.rest.mock_history.ResponseSourceNamespace.ResponseSource
import com.mocker.rest.mock_history.ResponseTimeSort
import com.mocker.rest.model.MockHistoryItem
import com.mocker.rest.request.{KVPair, Method}
import com.mocker.rest.utils.Implicits.MapAny
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Tag}

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

case class MySqlMockHistoryActions()(implicit ec: ExecutionContext) extends MockHistoryActions {

  private lazy val table = TableQuery[MockHistoryTable]

  override def get(serviceId: Long): DBIO[Seq[MockHistoryItem]] =
    table.filter(_.serviceId === serviceId).result

  override def deleteOldRecords(): DBIO[Unit] =
    table.filter(_.responseTime <= Instant.now().minusNanos(7.days.toNanos)).delete.map(_ => ())

  override def insert(item: MockHistoryItem): DBIO[Unit] =
    (table += item).map(_ => ())

  override def search(
      serviceId: Long,
      searchUrl: Option[String],
      from: Option[Instant],
      to: Option[Instant],
      statusCodes: Set[Int],
      responseSources: Set[ResponseSource],
      methods: Set[Method],
      sort: ResponseTimeSort,
      limit: Int,
      shift: Int
  ): DBIO[Seq[MockHistoryItem]] =
    table
      .filter(_.serviceId === serviceId)
      .mapIf(searchUrl.nonEmpty, _.filter(_.queryUrl.like(s"%${searchUrl.get}%")))
      .mapIf(from.nonEmpty, _.filter(_.responseTime >= from))
      .mapIf(to.nonEmpty, _.filter(_.responseTime <= to))
      .mapIf(statusCodes.nonEmpty, _.filter(_.statusCode.inSet(statusCodes)))
      .mapIf(responseSources.nonEmpty, _.filter(_.responseSource.inSet(responseSources)))
      .mapIf(methods.nonEmpty, _.filter(_.method.inSet(methods)))
      .sortBy { item =>
        sort match {
          case ResponseTimeSort.DESC => item.responseTime.desc
          case ResponseTimeSort.ASC  => item.responseTime.asc
          case _                     => item.responseTime.desc
        }
      }
      .drop(shift)
      .take(limit)
      .result

  override def count(
      serviceId: Long,
      searchUrl: Option[String],
      from: Option[Instant],
      to: Option[Instant],
      statusCodes: Set[Int],
      responseSources: Set[ResponseSource],
      methods: Set[Method]
  ): DBIO[Int] =
    table
      .filter(_.serviceId === serviceId)
      .mapIf(searchUrl.nonEmpty, _.filter(_.queryUrl.like(s"%${searchUrl.get}%")))
      .mapIf(from.nonEmpty, _.filter(_.responseTime >= from))
      .mapIf(to.nonEmpty, _.filter(_.responseTime <= to))
      .mapIf(statusCodes.nonEmpty, _.filter(_.statusCode.inSet(statusCodes)))
      .mapIf(responseSources.nonEmpty, _.filter(_.responseSource.inSet(responseSources)))
      .mapIf(methods.nonEmpty, _.filter(_.method.inSet(methods)))
      .size
      .result
}

object MySqlMockHistoryActions {

  class MockHistoryTable(tag: Tag) extends Table[MockHistoryItem](tag, "mock_response_history") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def serviceId: Rep[Long] = column[Long]("service_id")
    def method: Rep[Method] = column("method")
    def queryUrl: Rep[String] = column[String]("query_url")
    def responseUrl: Rep[String] = column[String]("response_url")
    def responseSource: Rep[ResponseSource] = column("response_source")
    def statusCode: Rep[Int] = column[Int]("status_code")
    def requestHeaders: Rep[Seq[KVPair]] = column("request_headers")
    def responseHeaders: Rep[Seq[KVPair]] = column("response_headers")
    def responseTime: Rep[Instant] = column("response_time", O.SqlType("TIMESTAMP"))
    def response: Rep[String] = column[String]("response")

    override def * : ProvenShape[MockHistoryItem] =
      (
        id,
        serviceId,
        method,
        queryUrl,
        responseUrl,
        responseSource,
        statusCode,
        requestHeaders,
        responseHeaders,
        responseTime,
        response
      ) <> ((MockHistoryItem.apply _).tupled, MockHistoryItem.unapply)
  }
}
