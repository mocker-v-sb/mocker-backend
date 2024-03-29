package com.mocker.services.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.models.rest.requests.mock_response._
import com.mocker.models.rest.responses.mock_response.{GetAllMockStaticResponsesResponse, GetMockStaticResponseResponse}
import com.mocker.models.rest.utils.AuthUtils
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import com.mocker.services.utils.Request.RequestOps
import com.mocker.services.utils.Response._
import io.grpc.{Status => GrpcStatus}
import zio.http._
import zio.http.model.Method.{DELETE, GET, POST, PUT}
import zio.http.model.{Status => HttpStatus}
import zio.json.{DecoderOps, EncoderOps}
import zio.{Cause, ZIO}

object MockRestApiMockResponseHandler {

  lazy val routes: Http[RestMockerClient.Service, Response, Request, Response] = Http
    .collectZIO[Request] {
      case req @ POST -> !! / "rest" / "service" / servicePath / "mock" / mockId / "response" =>
        mockId.toLongOption match {
          case Some(mockId) =>
            for {
              auth <- req.getUser.map(AuthUtils.buildAuthorization)
              request <- req.body.asString
                .map(
                  _.fromJson[CreateMockStaticResponseRequest].map(_.copy(servicePath = servicePath, mockId = mockId))
                )
                .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
              protoResponse <- (request match {
                case Right(request) => RestMockerClientService.createMockStaticResponse(request)(auth)
                case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
              }).either
              response <- protoResponse.toHttp
            } yield response
          case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
      case req @ PUT -> !! / "rest" / "service" / servicePath / "mock" / mockId / "response" / responseId =>
        (mockId.toLongOption, responseId.toLongOption) match {
          case (Some(mockId), Some(responseId)) =>
            for {
              auth <- req.getUser.map(AuthUtils.buildAuthorization)
              request <- req.body.asString
                .map(
                  _.fromJson[UpdateMockStaticResponseRequest]
                    .map(_.copy(servicePath = servicePath, mockId = mockId, responseId = responseId))
                )
                .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
              protoResponse <- (request match {
                case Right(request) => RestMockerClientService.updateMockStaticResponse(request)(auth)
                case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
              }).either
              response <- protoResponse.toHttp
            } yield response
          case _ => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
      case req @ DELETE -> !! / "rest" / "service" / servicePath / "mock" / mockId / "response" / responseId =>
        (mockId.toLongOption, responseId.toLongOption) match {
          case (Some(mockId), Some(responseId)) =>
            for {
              auth <- req.getUser.map(AuthUtils.buildAuthorization)
              protoResponse <- RestMockerClientService
                .deleteMockStaticResponse(DeleteMockStaticResponseRequest(servicePath, mockId, responseId))(auth)
                .either
              response <- protoResponse.toHttp
            } yield response
          case _ => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
      case req @ DELETE -> !! / "rest" / "service" / servicePath / "mock" / mockId / "responses" =>
        mockId.toLongOption match {
          case Some(mockId) =>
            for {
              auth <- req.getUser.map(AuthUtils.buildAuthorization)
              protoResponse <- RestMockerClientService
                .deleteAllMockStaticResponses(DeleteAllMockStaticResponsesRequest(servicePath, mockId))(auth)
                .either
              response <- protoResponse.toHttp
            } yield response
          case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
      case req @ GET -> !! / "rest" / "service" / servicePath / "mock" / mockId / "responses" =>
        mockId.toLongOption match {
          case Some(mockId) =>
            for {
              auth <- req.getUser.map(AuthUtils.buildAuthorization)
              protoResponse <- RestMockerClientService
                .getAllMockStaticResponses(GetAllMockStaticResponsesRequest(servicePath, mockId))(auth)
                .either
              response <- protoResponse.withBody(GetAllMockStaticResponsesResponse.fromMessage(_).toJson)
            } yield response
          case _ => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
      case req @ GET -> !! / "rest" / "service" / servicePath / "mock" / mockId / "response" / responseId =>
        (mockId.toLongOption, responseId.toLongOption) match {
          case (Some(mockId), Some(responseId)) =>
            for {
              auth <- req.getUser.map(AuthUtils.buildAuthorization)
              protoResponse <- RestMockerClientService
                .getMockStaticResponse(GetMockStaticResponseRequest(servicePath, mockId, responseId))(auth)
                .either
              response <- protoResponse.withBody(GetMockStaticResponseResponse.fromMessage(_).toJson)
            } yield response
          case _ => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
    }
    .tapErrorZIO(err => ZIO.logErrorCause(Cause.fail(err)))
    .mapError(_ => Response.status(HttpStatus.InternalServerError))
}
