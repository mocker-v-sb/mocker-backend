package com.mocker.gateway.routes.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.gateway.routes.utils.Response._
import com.mocker.models.rest.requests.mock_response.{
  CreateMockStaticResponseRequest,
  DeleteAllMockStaticResponsesRequest,
  DeleteMockStaticResponseRequest,
  GetAllMockStaticResponsesRequest,
  GetMockStaticResponseRequest,
  UpdateMockStaticResponseRequest
}
import com.mocker.models.rest.responses.mock_response.{GetAllMockStaticResponsesResponse, GetMockStaticResponseResponse}
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import io.grpc.{Status => GrpcStatus}
import zhttp.http.Method.{DELETE, GET, POST, PUT}
import zhttp.http.{Status => HttpStatus, _}
import zio.json.{DecoderOps, EncoderOps}
import zio.{Console, ZIO}

object MockRestApiMockResponseHandler {

  lazy val routes: Http[RestMockerClient.Service, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req @ POST -> !! / "rest" / "service" / servicePath / "mock" / mockId / "response" =>
      mockId.toLongOption match {
        case Some(mockId) =>
          for {
            request <- req.bodyAsString
              .map(_.fromJson[CreateMockStaticResponseRequest].map(_.copy(servicePath = servicePath, mockId = mockId)))
              .tapError(err => Console.printError(err).ignoreLogged)
            protoResponse <- (request match {
              case Right(request) => RestMockerClientService.createMockStaticResponse(request)
              case Left(error)    => Console.printError(error).ignoreLogged *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
            }).either
            response <- protoResponse.toHttp
          } yield response
        case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
    case req @ PUT -> !! / "rest" / "service" / servicePath / "mock" / mockId / "response" / responseId =>
      (mockId.toLongOption, responseId.toLongOption) match {
        case (Some(mockId), Some(responseId)) =>
          for {
            request <- req.bodyAsString
              .map(
                _.fromJson[UpdateMockStaticResponseRequest]
                  .map(_.copy(servicePath = servicePath, mockId = mockId, responseId = responseId))
              )
              .tapError(err => Console.printError(err).ignoreLogged)
            protoResponse <- (request match {
              case Right(request) => RestMockerClientService.updateMockStaticResponse(request)
              case Left(error)    => Console.printError(error).ignoreLogged *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
            }).either
            response <- protoResponse.toHttp
          } yield response
        case _ => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
    case req @ DELETE -> !! / "rest" / "service" / servicePath / "mock" / mockId / "response" / responseId =>
      (mockId.toLongOption, responseId.toLongOption) match {
        case (Some(mockId), Some(responseId)) =>
          for {
            protoResponse <- RestMockerClientService
              .deleteMockStaticResponse(DeleteMockStaticResponseRequest(servicePath, mockId, responseId))
              .either
            response <- protoResponse.toHttp
          } yield response
        case _ => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
    case req @ DELETE -> !! / "rest" / "service" / servicePath / "mock" / mockId / "responses" =>
      mockId.toLongOption match {
        case Some(mockId) =>
          for {
            protoResponse <- RestMockerClientService
              .deleteAllMockStaticResponses(DeleteAllMockStaticResponsesRequest(servicePath, mockId))
              .either
            response <- protoResponse.toHttp
          } yield response
        case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
    case req @ GET -> !! / "rest" / "service" / servicePath / "mock" / mockId / "responses" =>
      mockId.toLongOption match {
        case Some(mockId) =>
          for {
            protoResponse <- RestMockerClientService
              .getAllMockStaticResponses(GetAllMockStaticResponsesRequest(servicePath, mockId))
              .either
            response <- protoResponse.withJson(GetAllMockStaticResponsesResponse.fromMessage(_).toJson)
          } yield response
        case _ => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
    case req @ GET -> !! / "rest" / "service" / servicePath / "mock" / mockId / "response" / responseId =>
      (mockId.toLongOption, responseId.toLongOption) match {
        case (Some(mockId), Some(responseId)) =>
          for {
            protoResponse <- RestMockerClientService
              .getMockStaticResponse(GetMockStaticResponseRequest(servicePath, mockId, responseId))
              .either
            response <- protoResponse.withJson(GetMockStaticResponseResponse.fromMessage(_).toJson)
          } yield response
        case _ => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
  }
}
