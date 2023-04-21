package com.mocker.models.auth.responses

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class AuthenticationResponse(
    accessToken: String,
    refreshToken: String,
    email: String
)

object AuthenticationResponse {
  implicit val encoder = DeriveJsonEncoder.gen[AuthenticationResponse]
  implicit val decoder = DeriveJsonDecoder.gen[AuthenticationResponse]
}
