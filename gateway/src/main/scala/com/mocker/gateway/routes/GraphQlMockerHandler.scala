package com.mocker.gateway.routes

import com.mocker.common.utils.{Environment, ServerAddress}
import zio.{Cause, Console, ZIO}
import zio.http._
import zio.http.URL.Location
import zio.http.model.Scheme
import zio.http.model.{Status => HttpStatus}
import com.mocker.utils.LogAspect._

object GraphQlMockerHandler {

  private val serverAddress = ServerAddress(
    Environment.conf.getString("graphql-mocker-server.address"),
    Environment.conf.getInt("graphql-mocker-server.port")
  )

  lazy val routes: Http[Client, Response, Request, Response] = Http
    .collectZIO[Request] {
      case req @ _ -> "" /: "graphql" /: path => inner(req)
      case req @ _ -> "" /: "mocker" /: path  => inner(req)
    }
    .tapErrorZIO(err => ZIO.logErrorCause(Cause.fail(err)))
    .mapError(_ => Response.status(HttpStatus.InternalServerError))

  private def inner(request: Request) =
    for {
      url <- ZIO.succeed(
        URL(
          kind = Location.Absolute(
            scheme = Scheme.HTTP,
            host = serverAddress.address,
            port = serverAddress.port
          ),
          path = request.path,
          queryParams = request.url.queryParams
        )
      )
      proxiedRequest <- ZIO.succeed(
        Request.default(
          body = request.body,
          url = url,
          method = request.method
        )
      )
      response <- Client.request(proxiedRequest)
    } yield response
}
