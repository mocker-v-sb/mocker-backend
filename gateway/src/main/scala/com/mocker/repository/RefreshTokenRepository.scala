package com.mocker.repository

import com.mocker.models.auth.RefreshToken
import com.mocker.models.error.AppError.RepositoryError
import zio.ZIO

trait RefreshTokenRepository {
  def saveToken(token: RefreshToken): ZIO[RefreshTokenRepository, RepositoryError, Int]

  def findToken(token: String): ZIO[RefreshTokenRepository, RepositoryError, Option[RefreshToken]]

  def deleteToken(token: String): ZIO[RefreshTokenRepository, RepositoryError, Int]
}

object RefreshTokenRepository {

  def saveToken(token: RefreshToken): ZIO[RefreshTokenRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[RefreshTokenRepository](_.saveToken(token))

  def findToken(token: String): ZIO[RefreshTokenRepository, RepositoryError, Option[RefreshToken]] =
    ZIO.serviceWithZIO[RefreshTokenRepository](_.findToken(token))

  def deleteToken(token: String): ZIO[RefreshTokenRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[RefreshTokenRepository](_.deleteToken(token))
}
