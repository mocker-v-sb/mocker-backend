package com.mocker.rest

import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.rest.api.RestMockerService
import com.mocker.rest.manager.{
  RestHistoryManager,
  RestMockManager,
  RestMockResponseManager,
  RestModelManager,
  RestResponseManager,
  RestServiceManager
}
import com.mocker.rest.rest_service.ZioRestService.ZRestMocker
import com.mocker.rest.scheduler.RestExpiredServiceCleanerTask
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.{RequestContext, Server, ServerLayer, ServiceList}
import slick.interop.zio.DatabaseProvider
import zio.http.Client
import zio.redis._
import zio.schema._
import zio.schema.codec._
import zio.{Schedule, Scope, ZIO, ZIOAppArgs, ZLayer}

import scala.concurrent.ExecutionContext

object Main extends zio.ZIOAppDefault {

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(
    new java.util.concurrent.ForkJoinPool(1)
  )

  object ProtobufCodecSupplier extends CodecSupplier {
    def get[A: Schema]: BinaryCodec[A] = ProtobufCodec.protobufCodec
  }

  private val dbConfig = ZLayer.succeed(Environment.conf.getConfig("mysql.rest"))
  private val dbBackendLayer = ZLayer.succeed(slick.jdbc.H2Profile)

  private val redisConfig =
    ZLayer.succeed[RedisConfig](
      RedisConfig(Environment.conf.getString("redis.rest.host"), Environment.conf.getInt("redis.rest.port"))
    )

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

  private val dbProviderLayer = ZLayer.make[DatabaseProvider](
    dbConfig,
    dbBackendLayer,
    DatabaseProvider.fromConfig()
  )

  private val service = ZLayer.make[Server](
    dbProviderLayer,
    Client.default,
    redisConfig,
    RedisExecutor.layer,
    ZLayer.succeed[CodecSupplier](ProtobufCodecSupplier),
    Redis.layer,
    RestServiceManager.layer,
    RestModelManager.layer,
    RestMockManager.layer,
    RestMockResponseManager.layer,
    RestResponseManager.layer,
    RestHistoryManager.layer,
    RestMockerService.layer,
    serverLayer
  )

  private val restService =
    for {
      _ <- RestExpiredServiceCleanerTask
        .dropExpiredServices()
        .provide(dbProviderLayer)
        .schedule(Schedule.secondOfMinute(0))
        .forkDaemon
      _ <- service.launch
    } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = restService.exitCode
}
