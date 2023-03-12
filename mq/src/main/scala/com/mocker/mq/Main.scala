package com.mocker.mq

import com.mocker.common.utils.{Environment, ServerAddress}
import com.mocker.mq.mq_service.ZioMqService.ZMqMocker
import com.mocker.mq.adapters.{KafkaManager, MqMockerService}
import scalapb.zio_grpc.{RequestContext, Server, ServerLayer, ServiceList}
import zio.{durationInt, ZLayer}
import zio.kafka.admin.{AdminClient, AdminClientSettings}
import zio.kafka.producer.{Producer}

object Main extends zio.ZIOAppDefault {

  val mqServerAddress = ServerAddress(
    Environment.conf.getString("mq-server.address"),
    Environment.conf.getInt("mq-server.port")
  )

  val kafkaAddress = ServerAddress(
    Environment.conf.getString("kafka.address"),
    Environment.conf.getInt("kafka.port")
  )

  val serviceList = ServiceList.addFromEnvironment[ZMqMocker[RequestContext]]

  val serverLayer = ServerLayer.fromServiceList(
    io.grpc.ServerBuilder.forPort(mqServerAddress.port),
    serviceList
  )

  val producer = ZLayer.scoped(
    Producer.make(
      ProducerSettings(List(kafkaAddress))
    )
  )

  val adminClientSettings = ZLayer.succeed(
    AdminClientSettings(
      List("localhost:29092"),
      closeTimeout = 30.seconds,
      properties = Map[String, AnyRef]()
    )
  )

  val service = ZLayer.make[Server](
    producer,
    adminClientSettings,
    AdminClient.live,
    KafkaManager.layer,
    MqMockerService.layer,
    serverLayer
  )

  def run = service.launch.exitCode
}
