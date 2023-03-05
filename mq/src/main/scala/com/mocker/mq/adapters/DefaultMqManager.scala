package com.mocker.mq.adapters

import com.mocker.common.utils.ServerAddress
import com.mocker.mq.mq_service.BrokerInfoContainer.Container
import com.mocker.mq.mq_service.KafkaEvent.Value
import com.mocker.mq.mq_service._
import com.mocker.mq.ports.MqManager
import com.mocker.mq.utils.BrokerManagerException._
import com.mocker.mq.utils.{BrokerManagerException, BrokerType, KafkaController}
import org.apache.kafka.clients.producer.RecordMetadata
import zio.kafka.admin.AdminClient
import zio.kafka.consumer.Consumer
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.{IO, UIO, ZIO, ZLayer}

case class DefaultMqManager(kafkaController: KafkaController) extends MqManager {
  override def createTopic(request: CreateTopicRequest): IO[BrokerManagerException, CreateTopicResponse] = {
    def topicCreationException: BrokerManagerException =
      BrokerManagerException.CouldNotCreateTopic(request.topicName, BrokerType.Kafka)
    for {
      newTopic <- newTopic(request.topicName)
      _ <- kafkaController.adminClient.createTopic(newTopic).orElseFail(topicCreationException)
      topics <- kafkaController.adminClient.listTopics().orElseFail(topicCreationException)
      response <- ZIO.ifZIO(ZIO.succeed(topics.keySet.contains(request.topicName)))(
        onTrue = ZIO.succeed(
          CreateTopicResponse(
            host = kafkaController.address.address,
            port = kafkaController.address.port,
            topicName = request.topicName
          )
        ),
        onFalse = ZIO.fail(topicCreationException)
      )
    } yield response
  }

  override def sendMessage(request: SendMessageRequest): IO[BrokerManagerException, SendMessageResponse] = {
    def errorBuilder(topicName: String, brokerType: BrokerType = BrokerType.Unknown, reason: String): BrokerManagerException.CouldNotSendEvent =
      BrokerManagerException.CouldNotSendEvent(topicName, brokerType, reason)

    def kafkaErrorBuilder(topicName: String, reason: String): BrokerManagerException.CouldNotSendEvent =
      errorBuilder(topicName, BrokerType.Kafka, reason)
    for {
      meta <- request.brokerInfoContainer match {
        case Some(payload) =>
          payload.container match {
            case Container.KafkaContainer(container) => sendKafkaEvent(container, kafkaErrorBuilder)
            case Container.RabbitMqContainer(_) =>
              ZIO.fail(errorBuilder("UNKNOWN", BrokerType.RabbitMQ, "RabbitMq is not supported yet"))
            case Container.Empty => ZIO.fail(kafkaErrorBuilder("UNKNOWN", "Missing Container"))
          }
        case None => ZIO.fail(kafkaErrorBuilder("UNKNOWN", "Missing Payload"))
      }
    } yield SendMessageResponse(success = true)
  }

  override def getMessages(request: GetMessagesRequest): IO[CouldNotReadFromTopic, GetMessagesResponse] = {
    def errorBuilder(topicName: String, brokerType: BrokerType = BrokerType.Unknown, reason: String): BrokerManagerException.CouldNotReadFromTopic =
      BrokerManagerException.CouldNotReadFromTopic(topicName, brokerType, reason)

    def kafkaErrorBuilder(topicName: String, reason: String): BrokerManagerException.CouldNotReadFromTopic =
      errorBuilder(topicName, BrokerType.Kafka, reason)

    for {
      _ <- request.brokerMeta match {
        case Some(meta) => meta.container match {
          case Container.KafkaContainer(container) => container.content match {
            case Some(event) => ???
            case None => ZIO.fail(kafkaErrorBuilder("UNKNOWN", "Missing Container"))
          }
          case Container.RabbitMqContainer(_) => ZIO.fail(errorBuilder("UNKNOWN", BrokerType.RabbitMQ, "RabbitMq is not supported yet"))
          case Container.Empty => ZIO.fail(kafkaErrorBuilder("UNKNOWN", "Missing Container"))
        }
        case None => ZIO.fail(kafkaErrorBuilder("UNKNOWN", "Missing Payload"))
      }
    } yield GetMessagesResponse.defaultInstance
  }

  private def newTopic(topicName: String): UIO[AdminClient.NewTopic] =
    ZIO.succeed(AdminClient.NewTopic(topicName, 1, 1))

  private def sendKafkaEvent(
      container: KafkaContainer,
      errorBuilder: (String, String) => BrokerManagerException.CouldNotSendEvent
  ): IO[BrokerManagerException.CouldNotSendEvent, RecordMetadata] = {
    container.content match {
      case Some(content) =>
        content.value match {
          case Value.StringValue(value) =>
            kafkaController.producer
              .produce[Any, String, String](
                topic = container.topic,
                key = content.key,
                value = value,
                keySerializer = Serde.string,
                valueSerializer = Serde.string
              )
              .mapError { error =>
                errorBuilder(container.topic, error.getMessage)
              }
          case Value.Empty => ZIO.fail(errorBuilder(container.topic, "Missing Value"))
        }
      case None => ZIO.fail(errorBuilder(container.topic, "Missing Content"))
    }
  }
}

object DefaultMqManager {

  def layer: ZLayer[AdminClient with Producer with Consumer with ServerAddress, Nothing, DefaultMqManager] = {
    ZLayer.fromZIO {
      for {
        adminClient <- ZIO.service[AdminClient]
        producer <- ZIO.service[Producer]
        consumer <- ZIO.service[Consumer]
        brokerAddress <- ZIO.service[ServerAddress]
        kafkaController = KafkaController(adminClient, producer, consumer, brokerAddress)
      } yield DefaultMqManager(kafkaController)
    }
  }
}
