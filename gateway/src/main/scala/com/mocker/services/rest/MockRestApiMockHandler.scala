package com.mocker.services.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.models.rest.requests.mock._
import com.mocker.models.rest.responses.mock.{GetAllServiceMocksResponse, GetMockResponse}
import com.mocker.models.rest.utils.AuthUtils
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import com.mocker.services.utils.Request._
import com.mocker.services.utils.Response._
import io.grpc.{Status => GrpcStatus}
import zio.http._
import zio.http.model.Method.{DELETE, GET, PATCH, POST}
import zio.http.model.{Status => HttpStatus}
import zio.json.{DecoderOps, EncoderOps}
import zio.{Cause, ZIO}

object MockRestApiMockHandler {

  lazy val routes: Http[RestMockerClient.Service, Response, Request, Response] = Http
    .collectZIO[Request] {
      case req @ POST -> !! / "rest" / "service" / servicePath / "mock" =>
        for {
          auth <- req.getUser.map(AuthUtils.buildAuthorization)
          request <- req.body.asString
            .map(_.fromJson[CreateMockRequest].map(_.copy(servicePath = servicePath)))
            .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
          protoResponse <- (request match {
            case Right(request) => RestMockerClientService.createMock(request)(auth)
            case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse.toHttp
        } yield response
      case req @ PATCH -> !! / "rest" / "service" / servicePath / "mock" / mockId =>
        mockId.toLongOption match {
          case Some(mockId) =>
            for {
              auth <- req.getUser.map(AuthUtils.buildAuthorization)
              request <- req.body.asString
                .map(_.fromJson[UpdateMockRequest].map(_.copy(servicePath = servicePath, mockId = mockId)))
                .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
              protoResponse <- (request match {
                case Right(request) => RestMockerClientService.updateMock(request)(auth)
                case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
              }).either
              response <- protoResponse.toHttp
            } yield response
          case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
      case req @ DELETE -> !! / "rest" / "service" / servicePath / "mock" / mockId =>
        mockId.toLongOption match {
          case Some(mockId) =>
            for {
              auth <- req.getUser.map(AuthUtils.buildAuthorization)
              protoResponse <- RestMockerClientService.deleteMock(DeleteMockRequest(servicePath, mockId))(auth).either
              response <- protoResponse.toHttp
            } yield response
          case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
      case req @ DELETE -> !! / "rest" / "service" / servicePath / "mocks" =>
        for {
          auth <- req.getUser.map(AuthUtils.buildAuthorization)
          protoResponse <- RestMockerClientService
            .deleteServiceMocks(DeleteAllServiceMocksRequest(servicePath))(auth)
            .either
          response <- protoResponse.toHttp
        } yield response
      case req @ GET -> !! / "rest" / "service" / servicePath / "mocks" =>
        for {
          auth <- req.getUser.map(AuthUtils.buildAuthorization)
          protoResponse <- RestMockerClientService
            .getAllServiceMocks(GetAllServiceMocksRequest(servicePath))(auth)
            .either
          response <- protoResponse.withBody(GetAllServiceMocksResponse.fromMessage(_).toJson)
        } yield response
      case req @ GET -> !! / "rest" / "service" / servicePath / "mock" / mockId =>
        mockId.toLongOption match {
          case Some(mockId) =>
            for {
              auth <- req.getUser.map(AuthUtils.buildAuthorization)
              protoResponse <- RestMockerClientService.getMock(GetMockRequest(servicePath, mockId))(auth).either
              response <- protoResponse.withBody(GetMockResponse.fromMessage(_).toJson)
            } yield response
          case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
    }
    .tapErrorZIO(err => ZIO.logErrorCause(Cause.fail(err)))
    .mapError(_ => Response.status(HttpStatus.InternalServerError))
}
