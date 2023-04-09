package com.mocker.models.auth.requests

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class AuthorizationRequest(login: String, password: String)

object AuthorizationRequest {
  implicit val encoder = DeriveJsonEncoder.gen[AuthorizationRequest]
  implicit val decoder = DeriveJsonDecoder.gen[AuthorizationRequest]
}
