package com.mocker.repository

import com.mocker.models.auth.User
import com.mocker.models.error.AppError.RepositoryError
import zio.ZIO

trait AuthRepository {
  def findByEmail(email: String): ZIO[AuthRepository, RepositoryError, Option[User]]

  def insertUser(user: User): ZIO[AuthRepository, RepositoryError, Int]
}

object AuthRepository {

  def findByEmail(email: String): ZIO[AuthRepository, RepositoryError, Option[User]] =
    ZIO.serviceWithZIO[AuthRepository](_.findByEmail(email))

  def insertUser(user: User): ZIO[AuthRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[AuthRepository](_.insertUser(user))
}
