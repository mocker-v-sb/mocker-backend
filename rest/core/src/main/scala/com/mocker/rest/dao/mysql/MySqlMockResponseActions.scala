package com.mocker.rest.dao.mysql

import com.mocker.rest.dao.MockResponseActions
import com.mocker.rest.dao.implicits.MySqlImplicits._
import com.mocker.rest.dao.mysql.MySqlMockResponseActions.MockResponseTable
import com.mocker.rest.model.MockResponse
import com.mocker.rest.request.KVPair
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Rep, Tag}

import scala.concurrent.ExecutionContext

case class MySqlMockResponseActions()(implicit ec: ExecutionContext) extends MockResponseActions {

  private lazy val table = TableQuery[MockResponseTable]

  override def get(mockId: Long, responseId: Long): DBIO[Option[MockResponse]] =
    table.filter(_.mockId === mockId).filter(_.id === responseId).result.headOption

  override def getAll(mockId: Long): DBIO[Seq[MockResponse]] =
    table.filter(_.mockId === mockId).result

  override def upsert(mockResponse: MockResponse): DBIO[Unit] =
    table.insertOrUpdate(mockResponse).map(_ => ())

  override def delete(mockId: Long, responseId: Long): DBIO[Unit] =
    table.filter(_.mockId === mockId).filter(_.id === responseId).delete.map(_ => ())

  override def deleteAll(mockId: Long): DBIO[Unit] =
    table.filter(_.mockId === mockId).delete.map(_ => ())
}

object MySqlMockResponseActions {

  class MockResponseTable(tag: Tag) extends Table[MockResponse](tag, "mock_response") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def mockId: Rep[Long] = column[Long]("mock_id")
    def name: Rep[String] = column[String]("name")
    def statusCode: Rep[Int] = column[Int]("status_code")
    def requestHeaders: Rep[Set[KVPair]] = column("request_headers")
    def responseHeaders: Rep[Set[KVPair]] = column("response_headers")
    def pathParams: Rep[Set[KVPair]] = column("path_params")
    def queryParams: Rep[Set[KVPair]] = column("query_params")
    def response: Rep[String] = column[String]("response")

    override def * : ProvenShape[MockResponse] =
      (
        id,
        mockId,
        name,
        statusCode,
        requestHeaders,
        responseHeaders,
        pathParams,
        queryParams,
        response
      ) <> ((MockResponse.apply _).tupled, MockResponse.unapply)
  }
}
