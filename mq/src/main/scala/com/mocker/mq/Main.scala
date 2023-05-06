package com.mocker.mq

import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.mq.mq_service.ZioMqService.ZMqMocker
import com.mocker.mq.adapters.{MqManagerImpl, MqMockerService}
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}
import com.rabbitmq.tools.json
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.{RequestContext, Server, ServerLayer, ServiceList}
import zio.{durationInt, Task, ZIO, ZLayer}
import zio.kafka.admin.{AdminClient, AdminClientSettings}
import zio.kafka.producer.{Producer, ProducerSettings}

import scala.util.{Failure, Success, Try}

object Main extends zio.ZIOAppDefault {

  private def initializeConnection(): Task[Channel] =
    for {
      connection <- ZIO.attempt {
        val connectionFactory: ConnectionFactory = new ConnectionFactory()
        connectionFactory.setUsername(Environment.conf.getString("rabbitmq.username"))
        connectionFactory.setPassword(Environment.conf.getString("rabbitmq.password"))
        connectionFactory.setHost(Environment.conf.getString("rabbitmq.host"))
        connectionFactory.setPort(Environment.conf.getInt("rabbitmq.port"))
        connectionFactory.newConnection()
      }
      channel <- ZIO.attempt(connection.createChannel())
    } yield channel

  val mqServerAddress = ServerAddress(
    Environment.conf.getString("mq-server-server.address"),
    Environment.conf.getInt("mq-server-server.port")
  )

  val kafkaAddress = ServerAddress(
    Environment.conf.getString("kafka.address"),
    Environment.conf.getInt("kafka.port")
  )

  val publicKafkaAddress = ServerAddress(
    Environment.conf.getString("public-kafka.address"),
    Environment.conf.getInt("kafka.port")
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

  val service = ZLayer.make[Server](
    adminClientSettings,
    AdminClient.live,
    kafkaProducer,
    ZLayer.succeed(kafkaAddress),
    MqManagerImpl.layer,
    MqMockerService.layer,
    serverLayer
  )

  def run = service.launch.exitCode
}
