package com.mocker.gateway.routes.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.gateway.routes.utils.Response._
import com.mocker.models.rest.requests.mock.{
  CreateMockRequest,
  DeleteAllServiceMocksRequest,
  DeleteMockRequest,
  GetAllServiceMocksRequest,
  GetMockRequest,
  UpdateMockRequest
}
import com.mocker.models.rest.responses.mock.{GetAllServiceMocksResponse, GetMockResponse}
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import io.grpc.{Status => GrpcStatus}
import zhttp.http.Method.{DELETE, GET, POST, PUT}
import zhttp.http.{Status => HttpStatus, _}
import zio.json.{DecoderOps, EncoderOps}
import zio.{Console, ZIO}

object MockRestApiMockHandler {
  val prefix: Path = !! / "rest" / "service"

  lazy val routes: Http[RestMockerClient.Service, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req @ POST -> prefix / servicePath / "mock" =>
      for {
        request <- req.bodyAsString
          .map(_.fromJson[CreateMockRequest].map(_.copy(servicePath = servicePath)))
          .tapError(err => Console.printError(err).ignoreLogged)
        protoResponse <- (request match {
          case Right(request) => RestMockerClientService.createMock(request)
          case Left(error)    => Console.printError(error).ignoreLogged *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
        }).either
        response <- protoResponse.toHttp
      } yield response
    case req @ PUT -> prefix / servicePath / "mock" / mockId =>
      mockId.toLongOption match {
        case Some(mockId) =>
          for {
            request <- req.bodyAsString
              .map(_.fromJson[UpdateMockRequest].map(_.copy(servicePath = servicePath, mockId = mockId)))
              .tapError(err => Console.printError(err).ignoreLogged)
            protoResponse <- (request match {
              case Right(request) => RestMockerClientService.updateMock(request)
              case Left(error)    => Console.printError(error).ignoreLogged *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
            }).either
            response <- protoResponse.toHttp
          } yield response
        case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
    case req @ DELETE -> prefix / servicePath / "mock" / mockId =>
      mockId.toLongOption match {
        case Some(mockId) =>
          for {
            protoResponse <- RestMockerClientService.deleteMock(DeleteMockRequest(servicePath, mockId)).either
            response <- protoResponse.toHttp
          } yield response
        case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
    case req @ DELETE -> prefix / servicePath / "mocks" =>
      for {
        protoResponse <- RestMockerClientService.deleteServiceMocks(DeleteAllServiceMocksRequest(servicePath)).either
        response <- protoResponse.toHttp
      } yield response
    case req @ GET -> prefix / servicePath / "mock" / mockId =>
      mockId.toLongOption match {
        case Some(mockId) =>
          for {
            protoResponse <- RestMockerClientService.getMock(GetMockRequest(servicePath, mockId)).either
            response <- protoResponse.withJson(GetMockResponse.fromMessage(_).toJson)
          } yield response
        case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
    case req @ GET -> prefix / servicePath / "mocks" =>
      for {
        protoResponse <- RestMockerClientService.getAllServiceMocks(GetAllServiceMocksRequest(servicePath)).either
        response <- protoResponse.withJson(GetAllServiceMocksResponse.fromMessage(_).toJson)
      } yield response
  }
}