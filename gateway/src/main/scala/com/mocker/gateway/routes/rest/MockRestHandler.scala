package com.mocker.gateway.routes.rest

import com.mocker.clients.RestMockerClientService
import com.mocker.gateway.routes.utils.StatusMapper
import com.mocker.models.rest.common
import com.mocker.models.rest.common.KVPair
import com.mocker.models.rest.requests.GetResponseRequest
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import io.grpc.{Status => GrpcStatus}
import zhttp.http.Method._
import zhttp.http.{Response, Status => HttpStatus, _}
import zio.{IO, ZIO}

object MockRestHandler {

  lazy val routes: Http[RestMockerClient.Service, Throwable, Request, Response] = Http.collectZIO[Request] {
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
        body <- req.bodyAsString
        request <- buildRequest(path, req.headers, req.url.queryParams, Some(body)).either
        response <- request match {
          case Left(_)        => ZIO.succeed(Response.status(HttpStatus.BadRequest))
          case Right(request) => buildHttpResponse(request.copy(method = common.POST))
        }
      } yield response
    case req @ PUT -> "" /: "rest" /: path =>
      for {
        body <- req.bodyAsString
        request <- buildRequest(path, req.headers, req.url.queryParams, Some(body)).either
        response <- request match {
          case Left(_)        => ZIO.succeed(Response.status(HttpStatus.BadRequest))
          case Right(request) => buildHttpResponse(request.copy(method = common.PUT))
        }
      } yield response
    case req @ PATCH -> "" /: "rest" /: path =>
      for {
        body <- req.bodyAsString
        request <- buildRequest(path, req.headers, req.url.queryParams, Some(body)).either
        response <- request match {
          case Left(_)        => ZIO.succeed(Response.status(HttpStatus.BadRequest))
          case Right(request) => buildHttpResponse(request.copy(method = common.PATCH))
        }
      } yield response
  }

  private def buildRequest(
      path: Path,
      headers: Headers,
      queryParams: Map[String, List[String]]
  ): IO[Throwable, GetResponseRequest] = {
    buildRequest(path, headers, queryParams, None)
  }

  private def buildRequest(
      path: Path,
      headers: Headers,
      queryParams: Map[String, List[String]],
      body: Option[String]
  ): IO[Throwable, GetResponseRequest] = {
    val request = GetResponseRequest(
      servicePath = "",
      requestPath = "/",
      method = common.Method.default,
      body = body,
      headers = headers.toList.map { case (name, value)       => KVPair(name, value) }.toSet,
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
              Headers(mockResponse.headers.map(header => (header.name, header.value)))
            }

          ZIO.succeed(response)
        case Left(errSt: GrpcStatus) => ZIO.succeed(Response.status(StatusMapper.grpc2Http(errSt)))
      }
    } yield response
  }

}
