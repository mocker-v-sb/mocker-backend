package com.mocker.gateway.routes.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.gateway.routes.utils.Response._
import com.mocker.models.rest.requests._
import com.mocker.models.rest.responses._
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import io.grpc.{Status => GrpcStatus}
import zhttp.http.Method.{DELETE, GET, POST, PUT}
import zhttp.http.{Status => HttpStatus, _}
import zio.json.{DecoderOps, EncoderOps}
import zio.{Console, ZIO}

object MockRestApiModelHandler {
  val prefix: Path = !! / "rest" / "service"

  lazy val routes: Http[RestMockerClient.Service, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req @ POST -> prefix / servicePath / "model" =>
      for {
        request <- req.bodyAsString
          .map(_.fromJson[CreateModelRequest].map(_.copy(servicePath = servicePath)))
          .tapError(err => Console.printError(err).ignoreLogged)
        protoResponse <- (request match {
          case Right(request) => RestMockerClientService.createModel(request)
          case Left(error)    => Console.printError(error).ignoreLogged *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
        }).either
        response <- protoResponse.toHttp
      } yield response
    case req @ PUT -> prefix / servicePath / "model" / modelId =>
      modelId.toLongOption match {
        case Some(modelId) =>
          for {
            request <- req.bodyAsString
              .map(_.fromJson[UpdateModelRequest].map(_.copy(servicePath = servicePath, modelId = modelId)))
              .tapError(err => Console.printError(err).ignoreLogged)
            protoResponse <- (request match {
              case Right(request) => RestMockerClientService.updateModel(request)
              case Left(error)    => Console.printError(error).ignoreLogged *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
            }).either
            response <- protoResponse.toHttp
          } yield response
        case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
    case req @ DELETE -> prefix / servicePath / "model" / modelId =>
      modelId.toLongOption match {
        case Some(modelId) =>
          for {
            protoResponse <- RestMockerClientService.deleteModel(DeleteModelRequest(servicePath, modelId)).either
            response <- protoResponse.toHttp
          } yield response
        case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
    case req @ DELETE -> prefix / servicePath / "model" =>
      for {
        protoResponse <- RestMockerClientService.deleteServiceModels(DeleteAllServiceModelsRequest(servicePath)).either
        response <- protoResponse.toHttp
      } yield response
    case req @ GET -> prefix / servicePath / "model" / modelId =>
      modelId.toLongOption match {
        case Some(modelId) =>
          for {
            protoResponse <- RestMockerClientService.getModel(GetModelRequest(servicePath, modelId)).either
            response <- protoResponse.withJson(GetModelResponse.fromMessage(_).toJson)
          } yield response
        case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
      }
    case req @ GET -> prefix / servicePath / "models" =>
      for {
        protoResponse <- RestMockerClientService.getAllServiceModels(GetAllServiceModelsRequest(servicePath)).either
        response <- protoResponse.withJson(GetAllServiceModelsResponse.fromMessage(_).toJson)
      } yield response
  }
}
