package com.mocker.rest.dao.mysql

import com.mocker.rest.dao.implicits.MySqlImplicits._
import com.mocker.rest.dao.MockActions
import com.mocker.rest.dao.mysql.MySqlMockActions.MockTable
import com.mocker.rest.model.Mock
import com.mocker.rest.request.{Header, Method}
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Tag}

import java.sql.Timestamp
import scala.concurrent.ExecutionContext

case class MySqlMockActions()(implicit ec: ExecutionContext) extends MockActions {

  private lazy val table = TableQuery[MockTable]

  override def get(path: String): DBIO[Option[Mock]] =
    table.filter(_.path === path).result.headOption

  override def upsert(mock: Mock): DBIO[Unit] =
    table.insertOrUpdate(mock).map(_ => ())
}

object MySqlMockActions {

  class MockTable(tag: Tag) extends Table[Mock](tag, "mock") {

    def id: Rep[Long] = column[Long]("id")
    def serviceId: Rep[Long] = column[Long]("service_id")
    def name: Rep[String] = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")
    def path: Rep[String] = column[String]("path")
    def method: Rep[Method] = column("method")
    def requestModelId: Rep[Option[Long]] = column[Long]("request_model_id")
    def responseModelId: Rep[Option[Long]] = column[Long]("response_model_id")
    def requestHeaders: Rep[Seq[Header]] = column("request_headers")
    def responseHeaders: Rep[Seq[Header]] = column("response_headers")
    def queryParams: Rep[Seq[String]] = column("query_params")
    def pathParams: Rep[Seq[String]] = column("path_params")
    def creationTime: Rep[Timestamp] = column("creation_time", O.SqlType("TIMESTAMP"))

    def pk = primaryKey("pk_m", (id, serviceId))

    override def * : ProvenShape[Mock] =
      (
        id,
        serviceId,
        name,
        description,
        path,
        method,
        requestModelId,
        responseModelId,
        requestHeaders,
        responseHeaders,
        queryParams,
        pathParams,
        creationTime
      ) <> ((Mock.apply _).tupled, Mock.unapply)
  }
}
