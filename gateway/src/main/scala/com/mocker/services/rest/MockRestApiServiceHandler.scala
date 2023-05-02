package com.mocker.services.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.services.utils.Response._
import com.mocker.models.rest.requests.service._
import com.mocker.models.rest.responses.service._
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import io.grpc.{Status => GrpcStatus}
import zio.http.model.Method.{DELETE, GET, PATCH, POST, PUT}
import zio.http.model.{Status => HttpStatus}
import zio.http._
import zio.json.{DecoderOps, EncoderOps}
import zio.{Cause, Console, ZIO}

object MockRestApiServiceHandler {

  lazy val routes: Http[RestMockerClient.Service, Response, Request, Response] = Http
    .collectZIO[Request] {
      case req @ POST -> !! / "rest" / "service" =>
        for {
          request <- req.body.asString
            .map(_.fromJson[CreateServiceRequest])
            .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
          protoResponse <- (request match {
            case Right(request) => RestMockerClientService.createService(request)
            case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse.toHttp
        } yield response
      case req @ PUT -> !! / "rest" / "service" / servicePath =>
        for {
          request <- req.body.asString
            .map(_.fromJson[UpdateServiceRequest].map(_.copy(servicePath = servicePath)))
            .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
          protoResponse <- (request match {
            case Right(request) => RestMockerClientService.updateService(request)
            case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse.toHttp
        } yield response
      case req @ PATCH -> !! / "rest" / "service" / servicePath / "proxy" =>
        for {
          request <- req.body.asString
            .map(_.fromJson[SwitchServiceProxyRequest].map(_.copy(servicePath = servicePath)))
            .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
          protoResponse <- (request match {
            case Right(request) => RestMockerClientService.switchServiceProxy(request)
            case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse.toHttp
        } yield response
      case req @ PATCH -> !! / "rest" / "service" / servicePath / "history" =>
        for {
          request <- req.body.asString
            .map(_.fromJson[SwitchServiceHistoryRequest].map(_.copy(servicePath = servicePath)))
            .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
          protoResponse <- (request match {
            case Right(request) => RestMockerClientService.switchServiceHistory(request)
            case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse.toHttp
        } yield response
      case req @ DELETE -> !! / "rest" / "service" / servicePath =>
        for {
          protoResponse <- RestMockerClientService.deleteService(DeleteServiceRequest(servicePath)).either
          response <- protoResponse.toHttp
        } yield response
      case req @ GET -> !! / "rest" / "services" =>
        val search = req.url.queryParams.get("search").flatMap(_.headOption)
        for {
          result <- search match {
            case Some(query) => searchServices(query)
            case None        => getAllServices
          }
        } yield result
      case req @ GET -> !! / "rest" / "service" / servicePath =>
        for {
          protoResponse <- RestMockerClientService.getService(GetServiceRequest(servicePath)).either
          response <- protoResponse.withBody(GetServiceResponse.fromMessage(_).toJson)
        } yield response
    }
    .tapErrorZIO(err => ZIO.logErrorCause(Cause.fail(err)))
    .mapError(_ => Response.status(HttpStatus.InternalServerError))

  private def getAllServices = {
    for {
      protoResponse <- RestMockerClientService.getAllServices.either
      response <- protoResponse.withBody(GetAllServicesResponse.fromMessage(_).toJson)
    } yield response
  }

  private def searchServices(query: String) = {
    for {
      protoResponse <- RestMockerClientService.searchServices(SearchServicesRequest(query)).either
      response <- protoResponse.withBody(SearchServicesResponse.fromMessage(_).toJson)
    } yield response
  }
}
