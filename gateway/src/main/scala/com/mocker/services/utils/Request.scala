package com.mocker.services.utils

import com.mocker.models.auth.JwtContent
import com.mocker.services.AuthenticationService.jwtDecode
import zio.ZIO
import zio.http._
import zio.http.Request
import zio.http.model.Status
import zio.json.DecoderOps

object Request {

  implicit class RequestOps(private val request: Request) extends AnyVal {

    def getUser = {
      ZIO
        .fromOption {
          request.headers
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
            .text(
              s"could not parse auth header " +
                s"${request.headers.get("Authorization").map(_.split(" ").mkString(", "))}"
            )
            .setStatus(Status.BadRequest)
        )
    }
  }

}
