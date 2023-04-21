package com.mocker.models.auth.requests

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class RefreshTokenRequest(refreshToken: String)

object RefreshTokenRequest {
  implicit val encoder = DeriveJsonEncoder.gen[RefreshTokenRequest]
  implicit val decoder = DeriveJsonDecoder.gen[RefreshTokenRequest]
}
