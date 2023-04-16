package com.mocker.gateway.routes.utils

import io.grpc.{Status => GrpcStatus}
import zio.http.model.{Status => HttpStatus}
import zio.http.{Body, Response => ZIOResponse}
import zio.{UIO, ZIO}

object Response {

  implicit class EitherResponseImplicits[E, A](private val response: Either[E, A]) {

    def toHttp: UIO[ZIOResponse] = {
      response match {
        case Right(_)                => ZIO.succeed(ZIOResponse.status(HttpStatus.Ok))
        case Left(errSt: GrpcStatus) => ZIO.succeed(ZIOResponse.status(StatusMapper.grpc2Http(errSt)))
      }
    }

    def withBody(data: A => String): UIO[ZIOResponse] = {
      response match {
        case Right(value)            => ZIO.succeed(ZIOResponse(body = Body.fromString(data(value))).setStatus(HttpStatus.Ok))
        case Left(errSt: GrpcStatus) => ZIO.succeed(ZIOResponse.status(StatusMapper.grpc2Http(errSt)))
      }
    }
  }

}
