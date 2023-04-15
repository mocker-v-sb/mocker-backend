package com.mocker.gateway.routes.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.gateway.routes.utils.Response._
import com.mocker.models.rest.requests.model._
import com.mocker.models.rest.responses.model._
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import io.grpc.{Status => GrpcStatus}
import zio.http.model.Method.{DELETE, GET, POST, PUT}
import zio.http.model.{Status => HttpStatus}
import zio.http._
import zio.json.{DecoderOps, EncoderOps}
import zio.{Console, ZIO}

object MockRestApiModelHandler {

  lazy val routes: Http[RestMockerClient.Service, Response, Request, Response] = Http
    .collectZIO[Request] {
      case req @ POST -> !! / "rest" / "service" / servicePath / "model" =>
        for {
          request <- req.body.asString
            .map(_.fromJson[CreateModelRequest].map(_.copy(servicePath = servicePath)))
            .tapError(err => Console.printError(err).ignoreLogged)
          protoResponse <- (request match {
            case Right(request) => RestMockerClientService.createModel(request)
            case Left(error)    => Console.printError(error).ignoreLogged *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse.toHttp
        } yield response
      case req @ PUT -> !! / "rest" / "service" / servicePath / "model" / modelId =>
        modelId.toLongOption match {
          case Some(modelId) =>
            for {
              request <- req.body.asString
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
      case req @ DELETE -> !! / "rest" / "service" / servicePath / "model" / modelId =>
        modelId.toLongOption match {
          case Some(modelId) =>
            for {
              protoResponse <- RestMockerClientService.deleteModel(DeleteModelRequest(servicePath, modelId)).either
              response <- protoResponse.toHttp
            } yield response
          case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
      case req @ DELETE -> !! / "rest" / "service" / servicePath / "models" =>
        for {
          protoResponse <- RestMockerClientService
            .deleteServiceModels(DeleteAllServiceModelsRequest(servicePath))
            .either
          response <- protoResponse.toHttp
        } yield response
      case req @ GET -> !! / "rest" / "service" / servicePath / "models" =>
        for {
          protoResponse <- RestMockerClientService.getAllServiceModels(GetAllServiceModelsRequest(servicePath)).either
          response <- protoResponse.withBody(GetAllServiceModelsResponse.fromMessage(_).toJson)
        } yield response
      case req @ GET -> !! / "rest" / "service" / servicePath / "model" / modelId =>
        modelId.toLongOption match {
          case Some(modelId) =>
            for {
              protoResponse <- RestMockerClientService.getModel(GetModelRequest(servicePath, modelId)).either
              response <- protoResponse.withBody(GetModelResponse.fromMessage(_).toJson)
            } yield response
          case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
    }
    .tapErrorZIO(err => Console.printError(err).ignoreLogged)
    .mapError(_ => Response.status(HttpStatus.InternalServerError))

}
