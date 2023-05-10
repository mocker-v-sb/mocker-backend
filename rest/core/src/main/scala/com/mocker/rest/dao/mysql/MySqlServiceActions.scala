package com.mocker.rest.dao.mysql

import com.mocker.rest.dao.ServiceActions
import com.mocker.rest.dao.mysql.MySqlServiceActions.ServiceTable
import com.mocker.rest.model.{Service, ServiceStats}
import slick.dbio.DBIO
import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Tag}
import slick.sql.SqlProfile.ColumnOption.NotNull

import java.time.Instant
import scala.concurrent.ExecutionContext

case class MySqlServiceActions()(implicit ec: ExecutionContext) extends ServiceActions {

  private lazy val table = TableQuery[ServiceTable]

  implicit val getServiceStatsResult: GetResult[ServiceStats] = GetResult(
    r => ServiceStats(Service(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<), r.<<, r.<<)
  )

  implicit val gInstant: GetResult[Instant] = GetResult(
    r => Instant.parse(r.nextString())
  )

  implicit val gInstantOpt: GetResult[Option[Instant]] = GetResult(
    r => r.nextStringOption().map(Instant.parse)
  )

  override def getWithStats(user: String): DBIO[Seq[ServiceStats]] = {
    sql"""SELECT service.*,
           (SELECT COUNT(*) FROM mock where service.id = mock.service_id and service.owner = $user)   AS mock_count,
           (SELECT COUNT(*) FROM model where service.id = model.service_id and service.owner = $user) AS model_count
    FROM service;""".as[ServiceStats]
  }

  override def search(user: String, query: String): DBIO[Seq[ServiceStats]] = {
    sql"""SELECT service.*,
           (SELECT COUNT(*) FROM mock where service.id = mock.service_id and service.owner = $user)   AS mock_count,
           (SELECT COUNT(*) FROM model where service.id = model.service_id and service.owner = $user) AS model_count
    FROM service
        WHERE service.path LIKE '%#$query%' OR service.name LIKE '%#$query%';""".as[ServiceStats]
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

  override def deleteExpired(): DBIO[Unit] =
    sqlu"""DELETE FROM service WHERE expiration_time <= NOW();""".map(_ => ())
}

object MySqlServiceActions {

  class ServiceTable(tag: Tag) extends Table[Service](tag, "service") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name: Rep[String] = column[String]("name")
    def owner: Rep[String] = column[String]("owner")
    def path: Rep[String] = column[String]("path", O.Unique)
    def url: Rep[Option[String]] = column[Option[String]]("url")
    def description: Rep[Option[String]] = column[Option[String]]("description")
    def createTime: Rep[Instant] = column("creation_time", NotNull)
    def updateTime: Rep[Instant] = column("update_time", NotNull)
    def expirationTime: Rep[Option[Instant]] = column("expiration_time")
    def isProxyEnabled: Rep[Boolean] = column[Boolean]("proxy_enabled", NotNull)
    def isHistoryEnabled: Rep[Boolean] = column[Boolean]("history_enabled", NotNull)

    override def * : ProvenShape[Service] =
      (
        id,
        name,
        owner,
        path,
        url,
        description,
        createTime,
        updateTime,
        expirationTime,
        isProxyEnabled,
        isHistoryEnabled
      ) <>
        ((Service.apply _).tupled, Service.unapply)
  }
}
