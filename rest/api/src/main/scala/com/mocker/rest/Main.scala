package com.mocker.rest

import com.mocker.common.utils.{Database, Environment, ServerAddress}
import com.mocker.rest.api.RestMockerService
import com.mocker.rest.manager.RestMockerManager
import com.mocker.rest.rest_service.ZioRestService.ZRestMocker
import scalapb.zio_grpc.{RequestContext, Server, ServerLayer, ServiceList}
import zio.{Scope, ZIO, ZIOAppArgs, ZLayer}

object Main extends zio.ZIOAppDefault {

  private val databaseLayer = ZLayer.succeed(Database.connect(Environment.conf.getConfig("mysql.rest")))

  private val restServerAddress = ServerAddress(
    Environment.conf.getString("rest-server.address"),
    Environment.conf.getInt("rest-server.port")
  )

  private val serviceList = ServiceList.addFromEnvironment[ZRestMocker[RequestContext]]

  private val serverLayer = ServerLayer.fromServiceList(
    io.grpc.ServerBuilder.forPort(restServerAddress.port),
    serviceList
  )

  private val service = ZLayer.make[Server](
    databaseLayer,
    RestMockerManager.layer,
    RestMockerService.layer,
    serverLayer
  )

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = service.launch
}
