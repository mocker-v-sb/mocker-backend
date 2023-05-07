package com.mocker.mq

import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.mq.adapters.{RabbitMqController, KafkaController, MqManagerImpl, MqMockerService}
import com.mocker.mq.mq_service.ZioMqService.ZMqMocker
import com.rabbitmq.client.ConnectionFactory
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.{RequestContext, Server, ServerLayer, ServiceList}
import zio.http.Client
import zio.kafka.admin.{AdminClient, AdminClientSettings}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.{durationInt, ZIO, ZLayer}

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
    ZIO.attempt {
      val connectionFactory: ConnectionFactory = new ConnectionFactory()
      connectionFactory.setUsername(Environment.conf.getString("rabbitmq.username"))
      connectionFactory.setPassword(Environment.conf.getString("rabbitmq.password"))
      connectionFactory.setHost(rabbitmqAddress.host)
      connectionFactory.setPort(rabbitmqAddress.port)
      connectionFactory.newConnection().createChannel()
    }
  )

  val serviceList = ServiceList.addFromEnvironment[ZMqMocker[RequestContext]]

  val serverLayer = ServerLayer.fromServiceList(
    ServerBuilder
      .forPort(mqServerAddress.port)
      .addService(ProtoReflectionService.newInstance()),
    serviceList
  )

  val kafkaProducer = ZLayer.scoped(
    Producer.make(
      ProducerSettings(List(kafkaAddress))
    )
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
    kafkaProducer,
    ZLayer.succeed(kafkaAddress),
    KafkaController.live
  )

  val rabbitmqController = ZLayer.make[RabbitMqController](
    rabbitmqChannel,
    ZLayer.succeed(rabbitmqAddress),
    Client.default,
    RabbitMqController.live
  )

  val service = ZLayer.make[Server](
    kafkaController,
    rabbitmqController,
    MqManagerImpl.layer,
    MqMockerService.layer,
    serverLayer
  )

  def run = service.launch.exitCode
}
