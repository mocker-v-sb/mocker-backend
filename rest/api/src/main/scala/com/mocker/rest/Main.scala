package com.mocker.rest

import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.rest.api.RestMockerService
import com.mocker.rest.manager.RestMockerManager
import com.mocker.rest.rest_service.ZioRestService.ZRestMocker
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.{RequestContext, Server, ServerLayer, ServiceList}
import slick.interop.zio.DatabaseProvider
import zio.{Scope, ZIO, ZIOAppArgs, ZLayer}

import scala.concurrent.ExecutionContext

object Main extends zio.ZIOAppDefault {

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(
    new java.util.concurrent.ForkJoinPool(1)
  )

  private val dbConfig = ZLayer.succeed(Environment.conf.getConfig("mysql.rest"))
  private val dbBackendLayer = ZLayer.succeed(slick.jdbc.H2Profile)

  private val restServerAddress = ServerAddress(
    Environment.conf.getString("rest-server.address"),
    Environment.conf.getInt("rest-server.port")
  )

  private val serviceList = ServiceList.addFromEnvironment[ZRestMocker[RequestContext]]

  private val serverLayer = ServerLayer.fromServiceList(
    io.grpc.ServerBuilder
      .forPort(restServerAddress.port)
      .addService(ProtoReflectionService.newInstance()),
    serviceList
  )

  private val service = ZLayer.make[Server](
    dbConfig,
    dbBackendLayer,
    DatabaseProvider.fromConfig(),
    RestMockerManager.layer,
    RestMockerService.layer,
    serverLayer
  )

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = service.launch
}
