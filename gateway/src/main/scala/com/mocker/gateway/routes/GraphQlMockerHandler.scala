package com.mocker.gateway.routes

import com.mocker.clients.GraphQlMockerClient
import zhttp.http.Method.{DELETE, GET, PATCH, POST, PUT}
import zhttp.http.{->, Http, Path, Request, Response}
import zio.ZIO

case class GraphQlMockerHandler(client: GraphQlMockerClient) {

  lazy val routes: Http[Nothing, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req @ _ -> "" /: "graphql" /: path => inner(req)
    case req @ _ -> "" /: "user" /: path => inner(req)
  }

  private def inner(request: Request) = for {
    response <- client.proxyRequest(request)
    responseBody <- response.bodyAsString
    responseStatus <- ZIO.succeed(response.status)
  } yield Response.text(responseBody).setStatus(responseStatus)
}
