package com.mocker.services

import com.mocker.models.auth.User
import com.mocker.models.auth.requests.AuthenticationRequest
import com.mocker.repository.AuthRepository
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio._
import zio.http.model.{Method, Status => HttpStatus}
import zio.http._
import zio.json.DecoderOps

import java.security.SecureRandom
import java.time.Clock
import java.util.{Base64, UUID}
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

case class AuthenticationService(authRepository: AuthRepository) {
  private val SECRET_KEY = "secretKey"

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

  lazy val routes =
    Http
      .collectZIO[Request] {
        case Method.GET -> !! / "dummy-login" / username / password =>
          if (password.reverse.hashCode == username.hashCode)
            ZIO.succeed(Response.text(jwtEncode(username)))
          else
            ZIO.succeed(Response.text("Invalid username or password.").setStatus(HttpStatus.Unauthorized))

        case req @ Method.POST -> !! / "auth" / "signup" =>
          for {
            request <- req.body.asString
              .map(_.fromJson[AuthenticationRequest])
              .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
            response <- request match {
              case Right(req) =>
                authRepository.insertUser(User(
                  id = UUID.randomUUID(),
                  username = req.username,
                  password = hashPassword(req.password)
                ))
                .tapError(error => ZIO.logErrorCause(Cause.fail(error)))
                .mapBoth(_ => Response.text("DB error").setStatus(HttpStatus.InternalServerError), {
                  i =>
                    if (i > 0) Response.text("User Created successfully").setStatus(HttpStatus.Created)
                    else Response.text("Could not insert user").setStatus(HttpStatus.Conflict)
                })
              case Left(error) =>
                ZIO.logErrorCause(Cause.fail(error)) *>
                ZIO.succeed(Response.text("Could not parse request").setStatus(HttpStatus.BadRequest))
            }
          } yield response
      }
      .tapErrorZIO(err => ZIO.logErrorCause(Cause.fail(err)))
      .mapError(_ => Response.status(HttpStatus.InternalServerError))

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

      case other => sys.error("Bad password hash")
    }
  }
}

object AuthenticationService {

  def live =
    ZLayer.fromFunction(AuthenticationService.apply _)
}
