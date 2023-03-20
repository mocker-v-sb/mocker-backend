package com.mocker.gateway.routes.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.gateway.routes.utils.Response._
import com.mocker.models.rest.requests._
import com.mocker.models.rest.responses._
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import io.grpc.{Status => GrpcStatus}
import zhttp.http.Method.{DELETE, GET, POST, PUT}
import zhttp.http._
import zio.json.{DecoderOps, EncoderOps}
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
        response <- protoResponse.toHttp
      } yield response
    case req @ PUT -> prefix / servicePath =>
      for {
        request <- req.bodyAsString
          .map(_.fromJson[UpdateServiceRequest].map(_.copy(servicePath = servicePath)))
          .tapError(err => Console.printError(err).ignoreLogged)
        protoResponse <- (request match {
          case Right(request) => RestMockerClientService.updateService(request)
          case Left(error)    => Console.printError(error).ignoreLogged *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
        }).either
        response <- protoResponse.toHttp
      } yield response
    case req @ DELETE -> prefix / servicePath =>
      for {
        protoResponse <- RestMockerClientService.deleteService(DeleteServiceRequest(servicePath)).either
        response <- protoResponse.toHttp
      } yield response
    case req @ GET -> prefix / servicePath =>
      for {
        protoResponse <- RestMockerClientService.getService(GetServiceRequest(servicePath)).either
        response <- protoResponse.withJson(GetServiceResponse.fromMessage(_).toJson)
      } yield response
    case req @ GET -> !! / "rest" / "services" =>
      val search = req.url.queryParams.get("search").flatMap(_.headOption)
      search match {
        case Some(query) => searchServices(query)
        case None        => getAllServices
      }
  }

  private def getAllServices = {
    for {
      protoResponse <- RestMockerClientService.getAllServices.either
      response <- protoResponse.withJson(GetAllServicesResponse.fromMessage(_).toJson)
    } yield response
  }

  private def searchServices(query: String) = {
    for {
      protoResponse <- RestMockerClientService.searchServices(SearchServicesRequest(query)).either
      response <- protoResponse.withJson(SearchServicesResponse.fromMessage(_).toJson)
    } yield response
  }
}
