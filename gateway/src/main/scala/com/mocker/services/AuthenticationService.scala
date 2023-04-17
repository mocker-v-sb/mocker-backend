package com.mocker.services

import com.mocker.models.auth.requests.AuthenticationRequest
import com.mocker.repository.AuthRepository

import java.time.Clock
import zio._
import zio.http._
import zio.http.{Http, Request, Response}
import zio.http.model.{Method, Status}
import zio.http.model.{Status => HttpStatus}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio.json.DecoderOps
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.tracing.Tracing

case class AuthenticationService() {
  val SECRET_KEY = "secretKey"

  implicit val clock: Clock = Clock.systemUTC

  def jwtEncode(username: String): String = {
    val json = s"""{"user": "$username"}"""
    val claim = JwtClaim {
      json
    }.issuedNow.expiresIn(300)
    Jwt.encode(claim, SECRET_KEY, JwtAlgorithm.HS512)
  }

  def jwtDecode(token: String): Option[JwtClaim] = {
    Jwt.decode(token, SECRET_KEY, Seq(JwtAlgorithm.HS512)).toOption
  }

  def routes = Http.collectZIO[Request] {
    case Method.GET -> !! / "dummy-login" / username / password =>
      if (password.reverse.hashCode == username.hashCode)
        ZIO.succeed(Response.text(jwtEncode(username)))
      else
        ZIO.succeed(Response.text("Invalid username or password.").setStatus(Status.Unauthorized))

    case req @ Method.POST -> !! / "auth" / "signup" => for {
      request <- req.body.asString
        .map(_.fromJson[AuthenticationRequest])
        .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
    } yield Response.status(HttpStatus.Created)
  }
  .tapErrorZIO(err => ZIO.logErrorCause(Cause.fail(err)))
  .mapError(_ => Response.status(HttpStatus.InternalServerError))
}

object AuthenticationService {

  def live =
    ZLayer.fromFunction(AuthenticationService.apply _)
}
