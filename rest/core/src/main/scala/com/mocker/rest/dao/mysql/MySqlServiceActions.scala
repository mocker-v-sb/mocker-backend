package com.mocker.rest.dao.mysql

import com.mocker.rest.dao.ServiceActions
import com.mocker.rest.dao.mysql.MySqlServiceActions.ServiceTable
import com.mocker.rest.model.Service
import slick.dbio.DBIO
import slick.lifted.Tag
import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape
import slick.sql.SqlProfile.ColumnOption.NotNull

import java.time.Instant
import scala.concurrent.ExecutionContext

case class MySqlServiceActions()(implicit ec: ExecutionContext) extends ServiceActions {

  private lazy val table = TableQuery[ServiceTable]

  override def get(serviceId: Long): DBIO[Option[Service]] =
    table.filter(_.id === serviceId).result.headOption

  override def getAll: DBIO[Seq[Service]] =
    table.result

  override def upsert(service: Service): DBIO[Unit] =
    table.insertOrUpdate(service).map(_ => ())

  override def delete(serviceId: Long): DBIO[Unit] =
    table.filter(_.id === serviceId).delete.map(_ => ())
}

object MySqlServiceActions {

  class ServiceTable(tag: Tag) extends Table[Service](tag, "service") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def name: Rep[String] = column[String]("name", O.Unique)

    def url: Rep[Option[String]] = column[Option[String]]("url")

    def createTime: Rep[Instant] = column("create_time", NotNull, O.SqlType("TIMESTAMP"))

    def updateTime: Rep[Instant] = column("update_time", NotNull, O.SqlType("TIMESTAMP"))

    def expirationTime: Rep[Option[Instant]] = column("expiration_time", O.SqlType("TIMESTAMP"))

    override def * : ProvenShape[Service] =
      (id, name, url, createTime, updateTime, expirationTime) <> ((Service.apply _).tupled, Service.unapply)
  }
}
