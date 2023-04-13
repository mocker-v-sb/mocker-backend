package com.mocker.repository

import com.mocker.models.auth.User
import com.mocker.models.error.AppError.RepositoryError
import zio.sql.postgresql.PostgresJdbcModule
import zio.stream._
import zio._
import zio.schema.DeriveSchema

import java.util.UUID

trait PostgresTableDescription extends PostgresJdbcModule {

  implicit val userSchema = DeriveSchema.gen[User]

  val users = defineTableSmart[User]

  val (id, username, password) = users.columns

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
        username: String
    ): ZIO[Any, RepositoryError, T] =
      zstream.runHead.some
        .tapError {
          case None    => ZIO.unit
          case Some(e) => ZIO.logError(e.getMessage)
        }
        .mapError {
          case None =>
            RepositoryError(
              new RuntimeException(s"User with username $username does not exists")
            )
          case Some(e) => RepositoryError(e.getCause)
        }
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
