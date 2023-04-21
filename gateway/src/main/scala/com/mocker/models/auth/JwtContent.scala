package com.mocker.models.auth

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class JwtContent(user: String)

object JwtContent {
  implicit val encoder = DeriveJsonEncoder.gen[JwtContent]
  implicit val decoder = DeriveJsonDecoder.gen[JwtContent]
}
