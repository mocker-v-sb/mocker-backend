package com.mocker.services.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.common.auth.Authorization
import com.mocker.models.rest.common.{Method, ResponseSource, ResponseTimeSort}
import com.mocker.models.rest.requests.service._
import com.mocker.models.rest.responses.service._
import com.mocker.models.rest.utils.AuthUtils
import com.mocker.rest.rest_service.{CheckServiceExistence => ProtoCheckServiceExistence}
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import com.mocker.services.utils.Request.RequestOps
import com.mocker.services.utils.Response._
import io.grpc.{Status => GrpcStatus}
import zio.http._
import zio.http.model.Method._
import zio.http.model.{Status => HttpStatus}
import zio.json.{DecoderOps, EncoderOps}
import zio.{Cause, ZIO}

object MockRestApiServiceHandler {

  lazy val routes: Http[RestMockerClient.Service, Response, Request, Response] = Http
    .collectZIO[Request] {
      case req @ POST -> !! / "rest" / "service" =>
        for {
          auth <- req.getUser.map(AuthUtils.buildAuthorization)
          request <- req.body.asString
            .map(_.fromJson[CreateServiceRequest])
            .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
          protoResponse <- (request match {
            case Right(request) => RestMockerClientService.createService(request)(auth)
            case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse.toHttp
        } yield response
      case req @ PUT -> !! / "rest" / "service" / servicePath =>
        for {
          auth <- req.getUser.map(AuthUtils.buildAuthorization)
          request <- req.body.asString
            .map(_.fromJson[UpdateServiceRequest].map(_.copy(servicePath = servicePath)))
            .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
          protoResponse <- (request match {
            case Right(request) => RestMockerClientService.updateService(request)(auth)
            case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse.toHttp
        } yield response
      case req @ PATCH -> !! / "rest" / "service" / servicePath / "proxy" =>
        for {
          auth <- req.getUser.map(AuthUtils.buildAuthorization)
          request <- req.body.asString
            .map(_.fromJson[SwitchServiceProxyRequest].map(_.copy(servicePath = servicePath)))
            .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
          protoResponse <- (request match {
            case Right(request) => RestMockerClientService.switchServiceProxy(request)(auth)
            case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse.toHttp
        } yield response
      case req @ GET -> !! / "rest" / "service" / serviceId / "history" =>
        val params = req.url.queryParams

        val pageNum = params.get("page").flatMap(_.headOption).flatMap(_.toIntOption)
        val pageSize = params.get("pageSize").flatMap(_.headOption).flatMap(_.toIntOption)
        val from = params.get("from").flatMap(_.headOption).flatMap(_.toLongOption)
        val to = params.get("to").flatMap(_.headOption).flatMap(_.toLongOption)
        val search = params.get("search").flatMap(_.headOption)
        val statusCodes = params.get("statusCodes").getOrElse(Seq.empty).flatMap(_.toIntOption)
        val responseSources = params.get("responseSources").getOrElse(Seq.empty).flatMap(ResponseSource.forNameOpt)
        val requestMethods = params.get("requestMethods").getOrElse(Seq.empty).flatMap(Method.forNameOpt)
        val timeSort = params
          .get("timeSort")
          .flatMap(_.headOption)
          .flatMap(ResponseTimeSort.forNameOpt)
          .getOrElse(ResponseTimeSort.default)

        serviceId.toLongOption match {
          case Some(serviceId) =>
            ZIO.succeed(Response.status(HttpStatus.BadRequest))
            for {
              auth <- req.getUser.map(AuthUtils.buildAuthorization)
              protoResponse <- RestMockerClientService
                .getServiceResponseHistory(
                  GetServiceResponseHistoryRequest(
                    serviceId,
                    pageNum,
                    pageSize,
                    from,
                    to,
                    search,
                    statusCodes,
                    responseSources,
                    requestMethods,
                    timeSort
                  )
                )(auth)
                .either
              response <- protoResponse.withBody(GetServiceResponseHistoryResponse.fromMessage(_).toJson)
            } yield response
          case None => ZIO.succeed(Response.status(HttpStatus.BadRequest))
        }
      case req @ PATCH -> !! / "rest" / "service" / servicePath / "history" =>
        for {
          auth <- req.getUser.map(AuthUtils.buildAuthorization)
          request <- req.body.asString
            .map(_.fromJson[SwitchServiceHistoryRequest].map(_.copy(servicePath = servicePath)))
            .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
          protoResponse <- (request match {
            case Right(request) => RestMockerClientService.switchServiceHistory(request)(auth)
            case Left(error)    => ZIO.logErrorCause(Cause.fail(error)) *> ZIO.fail(GrpcStatus.INVALID_ARGUMENT)
          }).either
          response <- protoResponse.toHttp
        } yield response
      case req @ DELETE -> !! / "rest" / "service" / servicePath =>
        for {
          auth <- req.getUser.map(AuthUtils.buildAuthorization)
          protoResponse <- RestMockerClientService.deleteService(DeleteServiceRequest(servicePath))(auth).either
          response <- protoResponse.toHttp
        } yield response
      case req @ GET -> !! / "rest" / "services" =>
        val search = req.url.queryParams.get("search").flatMap(_.headOption)
        for {
          auth <- req.getUser.map(AuthUtils.buildAuthorization)
          result <- search match {
            case Some(query) => searchServices(query)(auth)
            case None        => getAllServices(auth)
          }
        } yield result
      case req @ GET -> !! / "rest" / "service" / servicePath =>
        for {
          auth <- req.getUser.map(AuthUtils.buildAuthorization)
          protoResponse <- RestMockerClientService.getService(GetServiceRequest(servicePath))(auth).either
          response <- protoResponse.withBody(GetServiceResponse.fromMessage(_).toJson)
        } yield response
      case req @ HEAD -> !! / "rest" / "service" / servicePath =>
        for {
          _ <- req.getUser
          protoResponse <- RestMockerClientService
            .checkServiceExistence(CheckServiceExistenceRequest(servicePath))
            .either
          response <- {
            protoResponse match {
              case Right(ProtoCheckServiceExistence.Response(true, _)) =>
                ZIO.succeed(Response.status(HttpStatus.Conflict))
              case _ => ZIO.succeed(Response.status(HttpStatus.Ok))
            }
          }
        } yield response
    }
    .tapErrorZIO(err => ZIO.logErrorCause(Cause.fail(err)))
    .mapError(_ => Response.status(HttpStatus.InternalServerError))

  private def getAllServices(auth: Authorization) = {
    for {
      protoResponse <- RestMockerClientService.getAllServices(auth).either
      response <- protoResponse.withBody(GetAllServicesResponse.fromMessage(_).toJson)
    } yield response
  }

  private def searchServices(query: String)(auth: Authorization) = {
    for {
      protoResponse <- RestMockerClientService.searchServices(SearchServicesRequest(query))(auth).either
      response <- protoResponse.withBody(SearchServicesResponse.fromMessage(_).toJson)
    } yield response
  }
}
