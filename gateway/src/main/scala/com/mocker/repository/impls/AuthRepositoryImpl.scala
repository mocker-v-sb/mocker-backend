package com.mocker.repository.impls

import com.mocker.models.auth.User
import com.mocker.models.error.AppError
import com.mocker.repository.{AuthRepository, PostgresTableDescription}
import zio._
import zio.sql.ConnectionPool

case class AuthRepositoryImpl(
    connectionPool: ConnectionPool
) extends AuthRepository
    with PostgresTableDescription {
  lazy val driverLayer = ZLayer
    .make[SqlDriver](
      SqlDriver.live,
      ZLayer.succeed(connectionPool)
    )

  override def findByEmail(_email: String): IO[AppError.RepositoryError, Option[User]] = {
    val query = select(userId, email, password)
      .from(users)
      .where(email === _email)

    ZIO.logInfo(s"Query to find user is ${renderRead(query)}") *>
      execute(query.to((User.apply _).tupled))
        .findFirst(driverLayer, _email)
  }

  override def insertUser(user: User): IO[AppError.RepositoryError, Int] = {
    val query = insertInto(users)(userId, email, password)
      .values(user.id, user.email, user.password)

    ZIO.logInfo(s"Query to insert user is ${renderInsert(query)}") *>
      execute(query).provideAndLog(driverLayer)
  }
}

object AuthRepositoryImpl {
  def live = ZLayer.fromFunction(AuthRepositoryImpl.apply _)
}
