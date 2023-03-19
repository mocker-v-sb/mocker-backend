package com.mocker.rest.dao.mysql

import com.mocker.rest.dao.ModelActions
import com.mocker.rest.dao.mysql.MySqlModelActions.ModelTable
import com.mocker.rest.model.Model
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.concurrent.ExecutionContext

case class MySqlModelActions()(implicit ec: ExecutionContext) extends ModelActions {

  private lazy val table = TableQuery[ModelTable]

  override def get(modelId: Long): DBIO[Option[Model]] =
    table.filter(_.id === modelId).result.headOption

  override def get(serviceId: Long, modelId: Long): DBIO[Option[Model]] =
    table.filter(_.serviceId === serviceId).filter(_.id === modelId).result.headOption

  override def getAll(serviceId: Long): DBIO[Seq[Model]] =
    table.filter(_.serviceId === serviceId).result

  override def upsert(model: Model): DBIO[Unit] =
    table.insertOrUpdate(model).map(_ => ())

  override def delete(serviceId: Long, modelId: Long): DBIO[Unit] =
    table.filter(_.serviceId === serviceId).filter(_.id === modelId).delete.map(_ => ())

  override def deleteAll(serviceId: Long): DBIO[Unit] =
    table.filter(_.serviceId === serviceId).delete.map(_ => ())
}

object MySqlModelActions {

  class ModelTable(tag: Tag) extends Table[Model](tag, "model") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def serviceId: Rep[Long] = column[Long]("service_id")
    def name: Rep[String] = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")
    def schema: Rep[String] = column[String]("schema")

    override def * : ProvenShape[Model] =
      (id, serviceId, name, description, schema) <> ((Model.apply _).tupled, Model.unapply)
  }
}
