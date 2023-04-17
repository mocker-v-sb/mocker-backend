package com.mocker.models.auth.requests

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class AuthenticationRequest(login: String, password: String)

object AuthenticationRequest {
  implicit val encoder = DeriveJsonEncoder.gen[AuthenticationRequest]
  implicit val decoder = DeriveJsonDecoder.gen[AuthenticationRequest]
}
