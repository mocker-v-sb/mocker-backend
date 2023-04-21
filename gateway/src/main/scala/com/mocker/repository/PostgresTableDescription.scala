package com.mocker.repository

import com.mocker.models.auth.{RefreshToken, User}
import com.mocker.models.error.AppError.RepositoryError
import zio.sql.postgresql.PostgresJdbcModule
import zio.stream._
import zio._
import zio.schema.DeriveSchema

import java.util.UUID

trait PostgresTableDescription extends PostgresJdbcModule {

  implicit val userSchema = DeriveSchema.gen[User]

  implicit val refreshTokenSchema = DeriveSchema.gen[RefreshToken]

  val users = defineTableSmart[User]

  val refreshTokens = defineTableSmart[RefreshToken]

  val (userId, email, password) = users.columns

  val (tokenId, token) = refreshTokens.columns

  implicit class ZStreamSqlExt[T](zstream: ZStream[SqlDriver, Exception, T]) {

    def provideDriver(
        driver: ULayer[SqlDriver]
    ): ZStream[Any, RepositoryError, T] =
      zstream
        .tapError(e => ZIO.logError(e.getMessage))
        .mapError(e => RepositoryError(e.getCause))
        .provideLayer(driver)

    def findFirst(
        driver: ULayer[SqlDriver],
        email: String
    ): ZIO[Any, RepositoryError, Option[T]] =
      zstream.runHead
        .tapError(error => ZIO.logError(s"Caught error while looking for user: $email\n${error.getMessage}"))
        .mapError(error => RepositoryError(error.getCause))
        .provide(driver)
  }

  implicit class ZioSqlExt[T](zio: ZIO[SqlDriver, Exception, T]) {

    def provideAndLog(driver: ULayer[SqlDriver]): ZIO[Any, RepositoryError, T] =
      zio
        .tapError(e => ZIO.logError(e.getMessage))
        .mapError(e => RepositoryError(e.getCause))
        .provide(driver)
  }
}
