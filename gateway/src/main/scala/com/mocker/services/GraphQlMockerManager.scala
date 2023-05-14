package com.mocker.services

import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.models.utils.UrlBuilder
import io.opentelemetry.api.trace.SpanKind
import zio.http.URL.Location
import zio.http._
import zio.http.model.Headers.Header
import zio.http.model.{Scheme, Status => HttpStatus}
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{Cause, ZIO, ZLayer}

case class GraphQlMockerManager(tracing: Tracing) {
  import com.mocker.services.GraphQlMockerManager._

  val gqlMockerAddress: ServerAddress = ServerAddress(
    Environment.conf.getString("graphql-mocker-server.address"),
    Environment.conf.getInt("graphql-mocker-server.port")
  )
  lazy val protectedRoutes: Http[Client, Response, Request, Response] = Http
    .collectZIO[Request] {
      case req @ _ -> "" /: "graphql" /: _ => proxy(req)
    }
    .tapErrorZIO(err => ZIO.logErrorCause(Cause.fail(err)))
    .mapError(_ => Response.status(HttpStatus.InternalServerError))
  lazy val routes: Http[Client, Response, Request, Response] = Http
    .collectZIO[Request] {
      case req @ _ -> "" /: "mocker" /: _ => proxy(req)
    }
    .tapErrorZIO(err => ZIO.logErrorCause(Cause.fail(err)))
    .mapError(_ => Response.status(HttpStatus.InternalServerError))

  private def proxy(request: Request) =
    tracing.span[Client, Throwable, Response](
      spanName = "proxy_graphql_request",
      spanKind = SpanKind.CLIENT
    )(inner(request))

  private def inner(request: Request) = {
    for {
      _ <- tracing.setAttribute("Method", request.method.toString())
      spanId <- tracing.getCurrentSpan.map(_.getSpanContext.getTraceId)
      url <- ZIO.succeed(
        URL(
          kind = Location.Absolute(
            scheme = Scheme.HTTP,
            host = gqlMockerAddress.host,
            port = gqlMockerAddress.port
          ),
          path = request.path,
          queryParams = request.url.queryParams
        )
      )
      _ <- tracing.setAttribute("url", UrlBuilder.asString(url))
      cTypeHeaderValue = request.headers.get(ContentTypeHeader).getOrElse(ApplicationJson)
      acceptHeaderValue = request.headers.get(AcceptHeader).getOrElse(ApplicationJson)
      authHeaderValue = request.headers.get(AuthorizationHeader).getOrElse("bearer empty")
      proxiedRequest <- ZIO.succeed(
        Request
          .default(
            body = request.body,
            url = url,
            method = request.method
          )
          .updateHeaders(
            _.combine(Header(RequestIdHeader, spanId))
              .combine(Header(AcceptHeader, acceptHeaderValue))
              .combine(Header(ContentTypeHeader, cTypeHeaderValue))
              .combine(Header(AuthorizationHeader, authHeaderValue))
          )
      )
      _ <- ZIO.foreach(proxiedRequest.headersAsList) { h =>
        tracing.setAttribute(h._1.toString, h._2.toString)
      }
      _ <- tracing.setAttribute("url", proxiedRequest.toString)
      response <- Client.request(proxiedRequest)
    } yield response
  }
}

object GraphQlMockerManager {
  val ContentTypeHeader = "Content-Type"
  val AuthorizationHeader = "Authorization"
  val RequestIdHeader = "x-request-id"
  val AcceptHeader = "Accept"
  val ApplicationJson = "application/json"

  def live: ZLayer[Tracing, Nothing, GraphQlMockerManager] =
    ZLayer.fromFunction(GraphQlMockerManager.apply _)
}
