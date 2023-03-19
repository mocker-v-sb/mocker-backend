package com.mocker.gateway.routes.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.gateway.routes.StatusMapper
import com.mocker.models.rest.requests.CreateServiceRequest
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import io.grpc.{Status => GrpcStatus}
import zhttp.http.Method.POST
import zhttp.http.{!!, ->, Http, Request, Response, Status => HttpStatus, _}
import zio.json.DecoderOps
import zio.{Console, ZIO}

object MockRestApiServiceHandler {
  val prefix: Path = !! / "rest" / "service"

  lazy val routes: Http[RestMockerClient.Service, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req @ POST -> prefix =>
      for {
        request <- req.bodyAsString
          .map(_.fromJson[CreateServiceRequest])
          .tapError(err => Console.printError(err).ignoreLogged)
        protoResponse <- (request match {
          case Right(request) => RestMockerClientService.createService(request)
          case Left(error)    => Console.printError(error).ignoreLogged *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
        }).either
        response <- protoResponse match {
          case Right(_)                => ZIO.succeed(Response.status(HttpStatus.Ok))
          case Left(errSt: GrpcStatus) => ZIO.succeed(Response.status(StatusMapper.grpc2Http(errSt)))
        }
      } yield response
  }
}
