package com.mocker.repository

import com.mocker.models.error.AppError
import zio.sql.ConnectionPool
import zio._
import zio.prelude.EqualOps
import com.mocker.models.auth.User

import java.util.UUID

final class AuthRepositoryImpl(
    connectionPool: ConnectionPool
) extends AuthRepository
    with PostgresTableDescription {
  lazy val driverLayer = ZLayer
    .make[SqlDriver](
      SqlDriver.live,
      ZLayer.succeed(connectionPool)
    )

  override def findByUsername(_username: String): IO[AppError.RepositoryError, User] = {
    val query = select(id, username, password)
      .from(users)
      .where(username === _username)

    ZIO.logInfo(s"Query to execute findByUsername is ${renderRead(query)}") *>
      execute(query.to((User.apply _).tupled))
        .findFirst(driverLayer, _username)
  }

  override def insertUser(user: User): IO[AppError.RepositoryError, Int] = {
    val _id = UUID.randomUUID()
    val query = (insertInto(users)(id, username, password))
      .values(_id, user.username, user.password)

    ZIO.logInfo(s"Query to insert user is ${renderInsert(query)}") *>
      execute(query).provideAndLog(driverLayer)
  }
}
