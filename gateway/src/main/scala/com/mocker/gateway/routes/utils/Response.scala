package com.mocker.gateway.routes.utils

import com.mocker.gateway.routes.StatusMapper
import io.grpc.{Status => GrpcStatus}
import zhttp.http.{Response => ZIOResponse, Status => HttpStatus}
import zio.{UIO, ZIO}

object Response {

  implicit class EitherResponseImplicits[E, A](private val response: Either[E, A]) {

    def toHttp: UIO[ZIOResponse] = {
      response match {
        case Right(_)                => ZIO.succeed(ZIOResponse.status(HttpStatus.Ok))
        case Left(errSt: GrpcStatus) => ZIO.succeed(ZIOResponse.status(StatusMapper.grpc2Http(errSt)))
      }
    }

    def withJson(json: A => String): UIO[ZIOResponse] = {
      response match {
        case Right(value)            => ZIO.succeed(ZIOResponse.json(json(value)).setStatus(HttpStatus.Ok))
        case Left(errSt: GrpcStatus) => ZIO.succeed(ZIOResponse.status(StatusMapper.grpc2Http(errSt)))
      }
    }
  }

}
