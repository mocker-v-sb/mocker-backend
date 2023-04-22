package com.mocker.repository.impls

import com.mocker.models.auth.RefreshToken
import com.mocker.models.error.AppError
import com.mocker.repository.{PostgresTableDescription, RefreshTokenRepository}
import zio.{ZIO, ZLayer}
import zio.sql.ConnectionPool

case class RefreshTokenRepositoryImpl(
    connectionPool: ConnectionPool
) extends RefreshTokenRepository
    with PostgresTableDescription {
  lazy val driverLayer = ZLayer
    .make[SqlDriver](
      SqlDriver.live,
      ZLayer.succeed(connectionPool)
    )

  override def saveToken(_token: RefreshToken): ZIO[RefreshTokenRepository, AppError.RepositoryError, Int] = {
    val query = insertInto(refreshTokens)(tokenId, token)
      .values(_token.id, _token.token)

    ZIO.logInfo(s"Query to insert refresh token is ${renderInsert(query)}") *>
      execute(query).provideAndLog(driverLayer)
  }

  override def findToken(
      _token: String
  ): ZIO[RefreshTokenRepository, AppError.RepositoryError, Option[RefreshToken]] = {
    val query = select(tokenId, token)
      .from(refreshTokens)
      .where(token === _token)

    ZIO.logInfo(s"Query to find refresh token is ${renderRead(query)}") *>
      execute(query.to((RefreshToken.apply _).tupled))
        .findFirst(driverLayer, _token)
  }

  override def deleteToken(_token: String): ZIO[RefreshTokenRepository, AppError.RepositoryError, Int] = {
    val query = deleteFrom(refreshTokens)
      .where(token === _token)

    ZIO.logInfo(s"Query to delete refresh token is ${renderDelete(query)}") *>
      execute(query).provideAndLog(driverLayer)
  }
}

object RefreshTokenRepositoryImpl {
  def live = ZLayer.fromFunction(RefreshTokenRepositoryImpl.apply _)
}
