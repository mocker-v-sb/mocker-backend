package com.mocker.rest.dao.mysql

import com.mocker.rest.dao.ServiceActions
import com.mocker.rest.dao.mysql.MySqlServiceActions.ServiceTable
import com.mocker.rest.model.{Service, ServiceStats}
import slick.dbio.DBIO
import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Tag}
import slick.sql.SqlProfile.ColumnOption.NotNull

import java.sql.Timestamp
import scala.concurrent.ExecutionContext

case class MySqlServiceActions()(implicit ec: ExecutionContext) extends ServiceActions {

  private lazy val table = TableQuery[ServiceTable]

  implicit val getServiceStatsResult: GetResult[ServiceStats] = GetResult(
    r => ServiceStats(Service(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<), r.<<, r.<<)
  )

  override def getWithStats: DBIO[Seq[ServiceStats]] = {
    sql"""SELECT service.*,
           (SELECT COUNT(mock.id)
            FROM service
                     LEFT JOIN mock ON service.id = mock.service_id)   AS mock_count,
           (SELECT COUNT(model.id)
            FROM service
                     LEFT JOIN model ON service.id = model.service_id) AS model_count
    FROM service
    GROUP BY service.id;""".as[ServiceStats]
  }

  override def search(query: String): DBIO[Seq[ServiceStats]] = {
    sql"""SELECT service.*,
           (SELECT COUNT(mock.id)
            FROM service
                     LEFT JOIN mock ON service.id = mock.service_id)   AS mock_count,
           (SELECT COUNT(model.id)
            FROM service
                     LEFT JOIN model ON service.id = model.service_id) AS model_count
    FROM service
        WHERE service.path LIKE '%#$query%' OR service.name LIKE '%#$query%'
    GROUP BY service.id;""".as[ServiceStats]
  }

  override def get(serviceId: Long): DBIO[Option[Service]] =
    table.filter(_.id === serviceId).result.headOption

  override def get(path: String): DBIO[Option[Service]] =
    table.filter(_.path === path).result.headOption

  override def getAll: DBIO[Seq[Service]] =
    table.result

  override def upsert(service: Service): DBIO[Unit] =
    table.insertOrUpdate(service).map(_ => ())

  override def delete(serviceId: Long): DBIO[Unit] =
    table.filter(_.id === serviceId).delete.map(_ => ())
}

object MySqlServiceActions {

  class ServiceTable(tag: Tag) extends Table[Service](tag, "service") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name: Rep[String] = column[String]("name")
    def path: Rep[String] = column[String]("path", O.Unique)
    def url: Rep[Option[String]] = column[Option[String]]("url")
    def description: Rep[Option[String]] = column[Option[String]]("description")
    def createTime: Rep[Timestamp] = column("creation_time", NotNull, O.SqlType("TIMESTAMP"))
    def updateTime: Rep[Timestamp] = column("update_time", NotNull, O.SqlType("TIMESTAMP"))
    def expirationTime: Rep[Option[Timestamp]] = column("expiration_time", O.SqlType("TIMESTAMP"))

    override def * : ProvenShape[Service] =
      (id, name, path, url, description, createTime, updateTime, expirationTime) <> ((Service.apply _).tupled, Service.unapply)
  }
}
