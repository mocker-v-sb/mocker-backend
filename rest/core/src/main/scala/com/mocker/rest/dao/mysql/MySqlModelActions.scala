package com.mocker.rest.dao.mysql

import com.mocker.rest.dao.ModelActions
import com.mocker.rest.dao.mysql.MySqlModelActions.ModelTable
import com.mocker.rest.model.Model
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Tag}
import slick.sql.SqlProfile.ColumnOption.NotNull

import java.sql.Timestamp
import scala.concurrent.ExecutionContext

case class MySqlModelActions()(implicit ec: ExecutionContext) extends ModelActions {

  private lazy val table = TableQuery[ModelTable]

  override def get(serviceId: Long, modelId: Long): DBIO[Option[Model]] =
    table.filter(_.serviceId === serviceId).filter(_.id === modelId).result.headOption

  override def upsert(model: Model): DBIO[Unit] =
    table.insertOrUpdate(model).map(_ => ())
}

object MySqlModelActions {

  class ModelTable(tag: Tag) extends Table[Model](tag, "model") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc)
    def serviceId: Rep[Long] = column[Long]("service_id")
    def name: Rep[String] = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")
    def createTime: Rep[Timestamp] = column("creation_time", NotNull, O.SqlType("TIMESTAMP"))

    def pk = primaryKey("pk_m", (id, serviceId))

    override def * : ProvenShape[Model] =
      (id, serviceId, name, description, createTime) <> ((Model.apply _).tupled, Model.unapply)
  }
}
