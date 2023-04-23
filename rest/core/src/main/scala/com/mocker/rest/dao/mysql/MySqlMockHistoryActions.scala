package com.mocker.rest.dao.mysql

import com.mocker.rest.dao.MockHistoryActions
import com.mocker.rest.dao.implicits.MySqlImplicits._
import com.mocker.rest.dao.mysql.MySqlMockHistoryActions.MockHistoryTable
import com.mocker.rest.mock_history.ResponseSourceNamespace.ResponseSource
import com.mocker.rest.model.MockHistoryItem
import com.mocker.rest.request.{KVPair, Method}
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.concurrent.ExecutionContext

case class MySqlMockHistoryActions()(implicit ec: ExecutionContext) extends MockHistoryActions {

  private lazy val table = TableQuery[MockHistoryTable]

  override def get(serviceId: Long): DBIO[Seq[MockHistoryItem]] =
    table.filter(_.serviceId === serviceId).result

  override def insert(item: MockHistoryItem): DBIO[Unit] =
    (table += item).map(_ => ())
}

object MySqlMockHistoryActions {

  class MockHistoryTable(tag: Tag) extends Table[MockHistoryItem](tag, "mock_response_history") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def serviceId: Rep[Long] = column[Long]("service_id")
    def method: Rep[Method] = column("method")
    def queryUrl: Rep[String] = column[String]("query_url")
    def responseUrl: Rep[String] = column[String]("response_url")
    def responseSource: Rep[ResponseSource] = column("response_url")
    def statusCode: Rep[Int] = column[Int]("status_code")
    def responseHeaders: Rep[Seq[KVPair]] = column("response_headers")
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
        responseHeaders,
        response
      ) <> ((MockHistoryItem.apply _).tupled, MockHistoryItem.unapply)
  }
}
