package com.mocker.gateway.routes

import com.mocker.common.utils.{Environment, ServerAddress}
import zhttp.http.{->, /:, Http, HttpData, Request, Response}
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.ZIO

object GraphQlMockerHandler {

  private val serverAddress = ServerAddress(
    Environment.conf.getString("graphql-mocker-server.address"),
    Environment.conf.getInt("graphql-mocker-server.port")
  )

  lazy val routes: Http[EventLoopGroup with ChannelFactory, Throwable, Request, Response] = Http.collectZIO[Request] {
    case req @ _ -> "" /: "graphql" /: path => inner(req)
    case req @ _ -> "" /: "mocker" /: path    => inner(req)
  }

  private def inner(request: Request) =
    for {
      content <- request.bodyAsString.map(b => HttpData.fromString(b))
      response <- Client.request(
        url = s"${serverAddress.toString}${request.path}?${request.url.queryParams.toSeq.mkString("&")}",
        method = request.method,
        content = content,
        headers = request.headers
      )
      responseBody <- response.bodyAsString
      responseStatus <- ZIO.succeed(response.status)
    } yield Response.json(responseBody).setStatus(responseStatus)
}
