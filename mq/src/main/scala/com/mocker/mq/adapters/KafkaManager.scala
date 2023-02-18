package com.mocker.mq.adapters

import com.mocker.mq.mq_service._
import com.mocker.mq.ports.MqManager
import zio.kafka.admin.AdminClient
import zio.kafka.producer.Producer
import zio.{Task, ZIO, ZLayer}

case class KafkaManager(adminClient: AdminClient, kafkaClient: Producer) extends MqManager {
  override def createTopic(request: CreateTopicRequest): Task[CreateTopicResponse] =
    for {
      newTopic <- newTopic(request.topicName)
      _ <- adminClient.createTopic(newTopic)
    } yield CreateTopicResponse.defaultInstance

  override def sendMessage(request: SendMessageRequest): Task[SendMessageResponse] = ???

  private def newTopic(topicName: String): Task[AdminClient.NewTopic] =
    ZIO.attempt(AdminClient.NewTopic(topicName, 1, 1))
}

object KafkaManager {
  def layer: ZLayer[AdminClient with Producer, Nothing, KafkaManager] = {
    ZLayer.fromZIO {
      for {
        kafkaAdminClient <- ZIO.service[AdminClient]
        kafkaProducerClient <- ZIO.service[Producer]
      } yield KafkaManager(kafkaAdminClient, kafkaProducerClient)
    }
  }
}
