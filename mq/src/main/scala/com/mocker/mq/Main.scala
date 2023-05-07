package com.mocker.mq

import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.mq.adapters.{KafkaController, MqManagerImpl, MqMockerService, RabbitMqController}
import com.mocker.mq.mq_service.ZioMqService.ZMqMocker
import com.rabbitmq.client.ConnectionFactory
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.{RequestContext, Server, ServerLayer, ServiceList}
import zio.http.Client
import zio.kafka.admin.{AdminClient, AdminClientSettings}
import zio.{Console, ZIO, ZLayer, durationInt}

import scala.util.Try

object Main extends zio.ZIOAppDefault {

  val mqServerAddress = ServerAddress(
    Environment.conf.getString("mq-server.address"),
    Environment.conf.getInt("mq-server.port")
  )

  val kafkaAddress = ServerAddress(
    Environment.conf.getString("kafka.address"),
    Environment.conf.getInt("kafka.port")
  )

  val rabbitmqAddress = ServerAddress(
    Environment.conf.getString("rabbitmq.host"),
    Environment.conf.getInt("rabbitmq.port")
  )

  val rabbitmqChannel = ZLayer.fromZIO(
    ZIO.fromTry {
      Try {
        val connectionFactory: ConnectionFactory = new ConnectionFactory()
        connectionFactory.setHost(rabbitmqAddress.host)
        connectionFactory.setPort(rabbitmqAddress.port)
        connectionFactory.newConnection().createChannel()
      }
    }.tapError(error => Console.printError(s"${error.getMessage}\n${error.getStackTrace.mkString("\n\t")}"))
  )

  val serviceList = ServiceList.addFromEnvironment[ZMqMocker[RequestContext]]

  val serverLayer = ServerLayer.fromServiceList(
    ServerBuilder
      .forPort(mqServerAddress.port)
      .addService(ProtoReflectionService.newInstance()),
    serviceList
  )

  val adminClientSettings = ZLayer.succeed(
    AdminClientSettings(
      List(kafkaAddress),
      closeTimeout = 30.seconds,
      properties = Map[String, AnyRef]()
    )
  )

  val kafkaController = ZLayer.make[KafkaController](
    adminClientSettings,
    AdminClient.live,
    ZLayer.succeed(kafkaAddress),
    zio.Scope.default,
    KafkaController.live
  )

  val rabbitmqController = ZLayer.make[RabbitMqController](
    rabbitmqChannel,
    ZLayer.succeed(rabbitmqAddress),
    Client.default,
    RabbitMqController.live
  )

  val service = ZLayer.make[Server](
    rabbitmqController,
    kafkaController,
    MqManagerImpl.layer,
    MqMockerService.layer,
    serverLayer
  )

  def run = service.launch.exitCode
}
