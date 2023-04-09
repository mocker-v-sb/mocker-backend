package com.mocker.gateway.routes

import java.time.Clock

import zio._

import zio.http.HttpAppMiddleware.bearerAuth
import zio.http._
import zio.http.model.{Method, Status}

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

object AuthenticationServer extends ZIOAppDefault {

  val SECRET_KEY = "secretKey"

  implicit val clock: Clock = Clock.systemUTC

  def jwtEncode(username: String): String = {
    val json  = s"""{"user": "${username}"}"""
    val claim = JwtClaim {
      json
    }.issuedNow.expiresIn(300)
    Jwt.encode(claim, SECRET_KEY, JwtAlgorithm.HS512)
  }

  def jwtDecode(token: String): Option[JwtClaim] = {
    Jwt.decode(token, SECRET_KEY, Seq(JwtAlgorithm.HS512)).toOption
  }

  def user: HttpApp[Any, Nothing] = Http.collect[Request] { case Method.GET -> !! / "user" / name / "greet" =>
    Response.text(s"Welcome to the ZIO party! ${name}")
  } @@ bearerAuth(jwtDecode(_).isDefined)

  def login: HttpApp[Any, Nothing] = Http.collect[Request] { case Method.GET -> !! / "login" / username / password  =>
    if (password.reverse.hashCode == username.hashCode) Response.text(jwtEncode(username))
    else Response.text("Invalid username or password.").setStatus(Status.Unauthorized)
  }

  val app: HttpApp[Any, Nothing] = login ++ user

  override val run = Server.serve(app).provide(Server.default)

}