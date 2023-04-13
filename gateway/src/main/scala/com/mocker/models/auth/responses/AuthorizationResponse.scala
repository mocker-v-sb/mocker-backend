package com.mocker.models.auth.responses

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class AuthorizationResponse(accessToken: String)

object AuthorizationResponse {
  implicit val encoder = DeriveJsonEncoder.gen[AuthorizationResponse]
  implicit val decoder = DeriveJsonDecoder.gen[AuthorizationResponse]
}
