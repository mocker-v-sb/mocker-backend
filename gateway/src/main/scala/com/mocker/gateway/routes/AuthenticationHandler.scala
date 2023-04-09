package com.mocker.gateway.routes

import java.time.Clock

import zio._
import zio.http._
import zio.http.model.{Method, Status}

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

//case class AuthenticationHandler(repository)
object AuthenticationHandler {

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

  def routes: HttpApp[Any, Nothing] = Http.collectZIO[Request] {
    case Method.GET -> !! / "dummy-login" / username / password =>
      if (password.reverse.hashCode == username.hashCode)
        ZIO.succeed(Response.text(jwtEncode(username)))
      else
        ZIO.succeed(Response.text("Invalid username or password.").setStatus(Status.Unauthorized))

    case Method.POST -> !! / "login" => ???
  }
}
