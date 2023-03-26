package com.mocker.clients

import com.mocker.common.utils.ServerAddress
import zhttp.http.{HttpData, Request, Response}
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.{URLayer, ZIO, ZLayer}

case class GraphQlMockerClient(serverAddress: ServerAddress) {
  def proxyRequest(request: Request): ZIO[EventLoopGroup with ChannelFactory, Throwable, Response] = for {
    content <- request.bodyAsString.map(b => HttpData.fromString(b))
    res <- Client.request(
      url = s"${serverAddress.toString}/${request.path}",
      method = request.method,
      content = content,
      headers = request.headers
    )
  } yield res
}

object GraphQlMockerClient {

  def layer: URLayer[ServerAddress, GraphQlMockerClient] = {
    ZLayer.fromZIO {
      for {
        serverAddress <- ZIO.service[ServerAddress]
      } yield GraphQlMockerClient(serverAddress)
    }
  }
}
