package com.mocker.mq.adapters

import com.mocker.common.utils.ServerAddress
import com.mocker.mq.adapters
import com.mocker.mq.mq_service.{BrokerType => ProtoBrokerType, _}
import com.mocker.mq.ports.MqManager
import com.mocker.mq.utils.{BrokerManagerException, BrokerType}
import com.rabbitmq.client.Channel
import io.grpc.Status
import zio.kafka.admin.AdminClient
import zio.kafka.producer.Producer
import zio.{IO, ZIO, ZLayer}

import scala.util.Random

case class MqManagerImpl(kafkaController: KafkaController, rabbitmqController: AmqpController) extends MqManager {
  override def createTopic(request: CreateTopicRequest): IO[BrokerManagerException, CreateTopicResponse] =
    request.brokerType match {
      case ProtoBrokerType.BROKER_TYPE_KAFKA    => kafkaController.createQueue(request)
      case ProtoBrokerType.BROKER_TYPE_RABBITMQ => rabbitmqController.createQueue(request)
      case _ =>
        ZIO.fail(
          BrokerManagerException.couldNotCreateTopic(
            request.topicName,
            BrokerType.Unknown,
            s"Broker type not defined",
            Status.INVALID_ARGUMENT
          )
        )
    }

  override def sendMessage(request: SendMessageRequest): IO[BrokerManagerException, SendMessageResponse] =
    request.brokerType match {
      case ProtoBrokerType.BROKER_TYPE_KAFKA    => kafkaController.sendMessages(request)
      case ProtoBrokerType.BROKER_TYPE_RABBITMQ => rabbitmqController.sendMessages(request)
      case _ =>
        ZIO.fail(
          BrokerManagerException.couldNotSendEvent(
            "UNKNOWN",
            BrokerType.Unknown,
            "Missing Broker Type",
            Status.INVALID_ARGUMENT
          )
        )
    }

  def getTopics(request: GetTopicsRequest): IO[BrokerManagerException, GetTopicsResponse] =
    request.brokerType match {
      case ProtoBrokerType.BROKER_TYPE_KAFKA    => kafkaController.getQueues(request)
      case ProtoBrokerType.BROKER_TYPE_RABBITMQ => rabbitmqController.getQueues(request)
      case _ =>
        kafkaController.getQueues(request).zipWithPar(rabbitmqController.getQueues(request)) { (a, b) =>
          GetTopicsResponse(Random.shuffle(a.queues ++ b.queues))
        }
    }

  def deleteTopic(request: DeleteTopicRequest): IO[BrokerManagerException, DeleteTopicResponse] =
    request.brokerType match {
      case ProtoBrokerType.BROKER_TYPE_KAFKA    => kafkaController.deleteTopic(request)
      case ProtoBrokerType.BROKER_TYPE_RABBITMQ => rabbitmqController.deleteTopic(request)
      case _ =>
        ZIO.fail(
          BrokerManagerException.couldNotDeleteTopic(
            request.topicName,
            BrokerType.Unknown,
            "Broker type not defined",
            Status.INVALID_ARGUMENT
          )
        )
    }

  override def getMessages(request: GetMessagesRequest): IO[BrokerManagerException, GetMessagesResponse] =
    request.brokerType match {
      case ProtoBrokerType.BROKER_TYPE_KAFKA    => kafkaController.getMessages(request)
      case ProtoBrokerType.BROKER_TYPE_RABBITMQ => rabbitmqController.getMessages(request)
      case _ =>
        ZIO.fail(
          BrokerManagerException.couldNotReadFromTopic(
            request.topic,
            BrokerType.Unknown,
            "Broker type not defined",
            Status.INVALID_ARGUMENT
          )
        )
    }
}

object MqManagerImpl {

  def layer: ZLayer[AdminClient with Producer with ServerAddress with Channel, Nothing, MqManagerImpl] = {
    ZLayer.fromZIO {
      for {
        adminClient <- ZIO.service[AdminClient]
        producer <- ZIO.service[Producer]
        brokerAddress <- ZIO.service[ServerAddress]
        channel <- ZIO.service[Channel]
        kafkaController = adapters.KafkaController(adminClient, producer, brokerAddress)
        rabbitmqController = adapters.AmqpController(channel)
      } yield MqManagerImpl(kafkaController, rabbitmqController)
    }
  }
}
