package com.mocker.gateway.routes.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.gateway.routes.utils.StatusMapper
import com.mocker.models.rest.common
import com.mocker.models.rest.common.KVPair
import com.mocker.models.rest.requests.GetResponseRequest
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import io.grpc.{Status => GrpcStatus}
import zio.{IO, ZIO}
import zio.http.model.Headers.Header
import zio.http.model.Headers
import zio.http.model.Method.{DELETE, GET, PATCH, POST, PUT}
import zio.http.model.{Status => HttpStatus}
import zio.http._
import zio.Console

object MockRestHandler {

  lazy val routes: Http[RestMockerClient.Service, Response, Request, Response] = Http
    .collectZIO[Request] {
      case req @ GET -> "" /: "rest" /: path =>
        for {
          request <- buildRequest(path, req.headers, req.url.queryParams).either
          response <- request match {
            case Left(_)        => ZIO.succeed(Response.status(HttpStatus.BadRequest))
            case Right(request) => buildHttpResponse(request.copy(method = common.GET))
          }
        } yield response
      case req @ DELETE -> "" /: "rest" /: path =>
        for {
          request <- buildRequest(path, req.headers, req.url.queryParams).either
          response <- request match {
            case Left(_)        => ZIO.succeed(Response.status(HttpStatus.BadRequest))
            case Right(request) => buildHttpResponse(request.copy(method = common.DELETE))
          }
        } yield response
      case req @ POST -> "" /: "rest" /: path =>
        for {
          body <- req.body.asString
          request <- buildRequest(path, req.headers, req.url.queryParams, Some(body)).either
          response <- request match {
            case Left(_)        => ZIO.succeed(Response.status(HttpStatus.BadRequest))
            case Right(request) => buildHttpResponse(request.copy(method = common.POST))
          }
        } yield response
      case req @ PUT -> "" /: "rest" /: path =>
        for {
          body <- req.body.asString
          request <- buildRequest(path, req.headers, req.url.queryParams, Some(body)).either
          response <- request match {
            case Left(_)        => ZIO.succeed(Response.status(HttpStatus.BadRequest))
            case Right(request) => buildHttpResponse(request.copy(method = common.PUT))
          }
        } yield response
      case req @ PATCH -> "" /: "rest" /: path =>
        for {
          body <- req.body.asString
          request <- buildRequest(path, req.headers, req.url.queryParams, Some(body)).either
          response <- request match {
            case Left(_)        => ZIO.succeed(Response.status(HttpStatus.BadRequest))
            case Right(request) => buildHttpResponse(request.copy(method = common.PATCH))
          }
        } yield response
    }
    .tapErrorZIO(err => Console.printError(err).ignoreLogged)
    .mapError(_ => Response.status(HttpStatus.InternalServerError))

  private def buildRequest(
      path: Path,
      headers: Headers,
      queryParams: QueryParams
  ): IO[Throwable, GetResponseRequest] = {
    buildRequest(path, headers, queryParams, None)
  }

  private def buildRequest(
      path: Path,
      headers: Headers,
      queryParams: QueryParams,
      body: Option[String]
  ): IO[Throwable, GetResponseRequest] = {
    val request = GetResponseRequest(
      servicePath = "",
      requestPath = "/",
      method = common.Method.default,
      body = body,
      headers = headers.toList.map { case Header(key, value)  => KVPair(key.toString, value.toString) }.toSet,
      queryParams = queryParams.flatMap { case (name, values) => values.map(value => KVPair(name, value)) }.toSet
    )
    path.encode.split("/").filter(_.nonEmpty).toList match {
      case Nil                => ZIO.fail(new IllegalArgumentException("Path can't be root"))
      case servicePath :: Nil => ZIO.succeed(request.copy(servicePath = servicePath))
      case servicePath :: requestPath =>
        ZIO.succeed(request.copy(servicePath = servicePath, requestPath = "/" + requestPath.mkString("/")))
    }
  }

  private def buildHttpResponse(request: GetResponseRequest) = {
    for {
      protoResponse <- RestMockerClientService.getResponse(request).either
      response <- protoResponse match {
        case Right(mockResponse) =>
          val response = Response
            .json(mockResponse.content)
            .setStatus(HttpStatus.Custom(mockResponse.statusCode))
            .setHeaders {
              Headers(mockResponse.headers.map(header => Header(header.name, header.value)))
            }

          ZIO.succeed(response)
        case Left(errSt: GrpcStatus) => ZIO.succeed(Response.status(StatusMapper.grpc2Http(errSt)))
      }
    } yield response
  }

}
