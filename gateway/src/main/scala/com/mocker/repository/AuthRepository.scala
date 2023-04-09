package com.mocker.repository

import com.mocker.models.auth.User
import com.mocker.models.error.AppError.RepositoryError
import zio.ZIO

trait AuthRepository {
  def findByUsername(username: String): ZIO[AuthRepository, RepositoryError, User]

  def insertUser(user: User): ZIO[AuthRepository, RepositoryError, Int]
}

object AuthRepository {

  def findByUsername(username: String): ZIO[AuthRepository, RepositoryError, User] =
    ZIO.serviceWithZIO[AuthRepository](_.findByUsername(username))

  def insertUser(user: User): ZIO[AuthRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[AuthRepository](_.insertUser(user))
}
