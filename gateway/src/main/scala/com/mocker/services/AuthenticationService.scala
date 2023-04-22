package com.mocker.services

import com.mocker.models.auth.{JwtContent, RefreshToken, User}
import com.mocker.models.auth.requests.{AuthenticationRequest, RefreshTokenRequest}
import com.mocker.models.auth.responses.AuthenticationResponse
import com.mocker.repository.{AuthRepository, RefreshTokenRepository}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtOptions}
import zio._
import zio.http.model.{Method, Status => HttpStatus}
import zio.http._
import zio.json.{DecoderOps, EncoderOps}

import java.security.SecureRandom
import java.time.Clock
import java.util.{Base64, UUID}
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

case class AuthenticationService(authRepository: AuthRepository, refreshTokenRepository: RefreshTokenRepository) {
  import com.mocker.services.AuthenticationService._

  lazy val routes: Http[RefreshTokenRepository with AuthRepository, Response, Request, Response] =
    Http
      .collectZIO[Request] {
        case req @ Method.POST -> !! / "auth" / "logout" =>
          for {
            requestE <- req.body.asString.mapBoth(
              _ => Response.status(HttpStatus.BadRequest),
              _.fromJson[RefreshTokenRequest]
            )
            request <- ZIO
              .fromEither(requestE)
              .tapError(err => ZIO.logError(err))
              .orElseFail(Response.status(HttpStatus.BadRequest))
            response <- refreshTokenRepository
              .deleteToken(request.refreshToken)
              .tapError(err => ZIO.logError(err.getMessage))
              .mapBoth(
                _ => Response.status(HttpStatus.InternalServerError),
                _ => Response.status(HttpStatus.Ok)
              )
          } yield response
        case req @ Method.POST -> !! / "auth" / "refresh" / "token" =>
          for {
            requestE <- req.body.asString
              .tapError(err => ZIO.logError(err.getMessage))
              .mapBoth(
              _ => Response.text("could not parse request1").setStatus(HttpStatus.BadRequest),
              _.fromJson[RefreshTokenRequest]
            )
            request <- ZIO
              .fromEither(requestE)
              .tapError(err => ZIO.logError(err))
              .orElseFail(Response.text("could not parse request1").setStatus(HttpStatus.BadRequest))
            email <- ZIO
              .fromOption {
                req.headers
                  .get("Authorization")
                  .flatMap { h =>
                    h.split(" ") match {
                      case Array(_, token) => Some(token)
                      case _               => None
                    }
                  }
                  .flatMap(t => jwtDecode(t, shouldIgnoreTiming = true))
                  .flatMap(_.content.fromJson[JwtContent].toOption)
                  .map(_.user)
              }
              .orElseFail(
                Response
                  .text(s"could not parse auth header " +
                    s"${req.headers.get("Authorization").map(_.split(" ").mkString(", "))}")
                  .setStatus(HttpStatus.BadRequest)
              )
            response <- refreshTokenRepository
              .findToken(request.refreshToken)
              .map {
                case Some(refreshToken) =>
                  Response
                    .json(
                      AuthenticationResponse(
                        accessToken = generateAccessToken(email),
                        refreshToken = refreshToken.token,
                        email = email
                      ).toJson
                    )
                    .setStatus(HttpStatus.Ok)
                case None => Response.status(HttpStatus.NotFound)
              }
              .tapError(err => ZIO.logError(err.getMessage))
              .orElseFail(Response.text("refreshTokenRepository error").setStatus(HttpStatus.InternalServerError))
          } yield response
        case req @ Method.POST -> !! / "auth" / "signup" =>
          for {
            requestE <- req.body.asString.mapBoth(
              _ => Response.status(HttpStatus.BadRequest),
              _.fromJson[AuthenticationRequest]
            )
            request <- ZIO
              .fromEither(requestE)
              .tapError(e => ZIO.logError(e))
              .orElseFail(Response.status(HttpStatus.BadRequest))
            response <- authRepository
              .insertUser(
                User(
                  id = UUID.randomUUID(),
                  email = request.email,
                  password = hashPassword(request.password)
                )
              )
              .tapError(error => ZIO.logError(error.cause.getMessage))
              .mapBoth(
                _ => Response.status(HttpStatus.InternalServerError), { i =>
                  if (i > 0) Response.status(HttpStatus.Ok)
                  else Response.status(HttpStatus.Conflict)
                }
              )
          } yield response
        case req @ Method.POST -> !! / "auth" / "login" =>
          for {
            requestE <- req.body.asString.mapBoth(
              _ => Response.status(HttpStatus.BadRequest),
              _.fromJson[AuthenticationRequest]
            )
            request <- ZIO
              .fromEither(requestE)
              .tapError(e => ZIO.logError(e))
              .orElseFail(Response.status(HttpStatus.BadRequest))
            userInfo <- authRepository
              .findByEmail(request.email)
              .tapError(err => ZIO.logError(err.getMessage))
              .orElseFail(Response.status(HttpStatus.InternalServerError))
            response <- userInfo
              .map { ui =>
                if (checkPassword(request.password, ui.password)) {
                  val uuid = UUID.randomUUID()
                  val rt = RefreshToken(
                    uuid,
                    generateRefreshToken(uuid.toString)
                  )
                  refreshTokenRepository
                    .saveToken(rt)
                    .tapError(err => ZIO.logError(err.cause.getMessage))
                    .mapBoth(
                      _ => Response.status(HttpStatus.NotFound),
                      _ =>
                        Response.json(
                          AuthenticationResponse(
                            generateAccessToken(request.email),
                            rt.token,
                            request.email
                          ).toJson
                        )
                    )
                } else {
                  ZIO.succeed(Response.status(HttpStatus.Unauthorized))
                }
              }
              .getOrElse(ZIO.succeed(Response.status(HttpStatus.NotFound)))
          } yield response
      }
}

object AuthenticationService {
  private val SECRET_KEY = "secretKey"

  implicit val clock: Clock = Clock.systemUTC

  def generateAccessToken(email: String): String = {
    val json = s"""{"user": "$email"}"""
    val claim = JwtClaim(json).issuedNow
      .expiresIn(60)
    Jwt.encode(claim, SECRET_KEY, JwtAlgorithm.HS512)
  }

  def generateRefreshToken(salt: String): String = {
    val json = s"""{"salt": "$salt"}"""
    val claim = JwtClaim {
      json
    }.issuedNow
    Jwt.encode(claim, SECRET_KEY, JwtAlgorithm.HS512)
  }

  def jwtDecode(token: String, shouldIgnoreTiming: Boolean = false): Option[JwtClaim] = {
    Jwt.decode(
      token,
      SECRET_KEY,
      Seq(JwtAlgorithm.HS512),
      if (shouldIgnoreTiming) JwtOptions(expiration = false, notBefore = false) else JwtOptions.DEFAULT
    ).toOption
  }

  private val DefaultIterations = 10000
  private val random = new SecureRandom()

  private def pbkdf2(password: String, salt: Array[Byte], iterations: Int): Array[Byte] = {
    val keySpec = new PBEKeySpec(password.toCharArray, salt, iterations, 256)
    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    keyFactory.generateSecret(keySpec).getEncoded
  }

  private def hashPassword(password: String): String = {
    val salt = new Array[Byte](16)
    random.nextBytes(salt)
    val hash = pbkdf2(password, salt, DefaultIterations)
    val hash64 = Base64.getEncoder.encodeToString(hash)
    val salt64 = Base64.getEncoder.encodeToString(salt)

    s"$DefaultIterations:$hash64:$salt64"
  }

  private def checkPassword(password: String, passwordHash: String): Boolean = {
    passwordHash.split(":") match {
      case Array(it, hash64, salt64) if it.forall(_.isDigit) =>
        val hash = Base64.getDecoder.decode(hash64)
        val salt = Base64.getDecoder.decode(salt64)

        val calculatedHash = pbkdf2(password, salt, it.toInt)
        calculatedHash.sameElements(hash)

      case _ => sys.error("Bad password hash")
    }
  }

  def live: ZLayer[AuthRepository with RefreshTokenRepository, Nothing, AuthenticationService] =
    ZLayer.fromFunction(AuthenticationService.apply _)
}
