package com.mocker.mq.adapters

import com.mocker.common.utils.ServerAddress
import com.mocker.mq.mq_service.BrokerInfoContainer.Container
import com.mocker.mq.mq_service.KafkaEvent.Value
import com.mocker.mq.mq_service.{BrokerType => ProtoBrokerType, _}
import com.mocker.mq.ports.MqManager
import com.mocker.mq.utils.{BrokerManagerException, BrokerType, KafkaController}
import io.grpc.Status
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.RecordMetadata
import zio.kafka.admin.AdminClient
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.{Cause, Console, IO, UIO, ZIO, ZLayer}

import java.time.Duration
import java.util.Properties
import scala.collection.mutable
import scala.jdk.CollectionConverters._

case class DefaultMqManager(kafkaController: KafkaController) extends MqManager {

  private val props: Properties = new Properties()
  props.put("bootstrap.servers", s"${kafkaController.address}")
  props.put("group.id", "kafka-mocker")
  props.put("enable.auto.commit", "true")
  props.put("auto.commit.interval.ms", "1000")
  props.put("session.timeout.ms", "30000")
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  override def createTopic(request: CreateTopicRequest): IO[BrokerManagerException, CreateTopicResponse] = {
    def topicCreationException(reason: String = "reason unknown", status: Status): BrokerManagerException =
      BrokerManagerException.couldNotCreateTopic(request.topicName, BrokerType.Kafka, reason, status)
    for {
      response <- request.brokerType match {
        case ProtoBrokerType.BROKER_TYPE_KAFKA => createKafkaTopic(request)
        case ProtoBrokerType.BROKER_TYPE_RABBITMQ =>
          ZIO.fail(topicCreationException("RabbitMQ support is not implemented yet", Status.UNIMPLEMENTED))
        case ProtoBrokerType.Unrecognized(unrecognizedValue) =>
          ZIO.fail(topicCreationException(s"Unknown broker type: $unrecognizedValue", Status.INVALID_ARGUMENT))
        case _ => ZIO.fail(topicCreationException(s"Broker type not defined", Status.INVALID_ARGUMENT))
      }
    } yield response
  }

  override def sendMessage(request: SendMessageRequest): IO[BrokerManagerException, SendMessageResponse] = {
    def errorBuilder(
        topicName: String,
        brokerType: BrokerType = BrokerType.Unknown,
        reason: String,
        grpcStatus: Status
    ): BrokerManagerException =
      BrokerManagerException.couldNotSendEvent(topicName, brokerType, reason, grpcStatus)

    def kafkaErrorBuilder(
        topicName: String,
        reason: String,
        grpcStatus: Status
    ): BrokerManagerException =
      errorBuilder(topicName, BrokerType.Kafka, reason, grpcStatus)
    for {
      _ <- request.brokerInfoContainer match {
        case Some(payload) =>
          payload.container match {
            case Container.KafkaContainer(container) => sendKafkaEvent(container, kafkaErrorBuilder, request.repeat)
            case Container.RabbitMqContainer(_) =>
              ZIO.fail(
                errorBuilder("UNKNOWN", BrokerType.RabbitMQ, "RabbitMq is not supported yet", Status.UNIMPLEMENTED)
              )
            case Container.Empty => ZIO.fail(kafkaErrorBuilder("UNKNOWN", "Missing Container", Status.INVALID_ARGUMENT))
          }
        case None => ZIO.fail(kafkaErrorBuilder("UNKNOWN", "Missing Payload", Status.INVALID_ARGUMENT))
      }
    } yield SendMessageResponse(success = true)
  }

  override def getMessages(request: GetMessagesRequest): IO[BrokerManagerException, GetMessagesResponse] = {
    def errorBuilder(
        topicName: String,
        brokerType: BrokerType = BrokerType.Unknown,
        reason: String,
        grpcStatus: Status
    ): BrokerManagerException =
      BrokerManagerException.couldNotReadFromTopic(topicName, brokerType, reason, grpcStatus)

    def kafkaErrorBuilder(
        topicName: String,
        reason: String,
        grpcStatus: Status
    ): BrokerManagerException =
      errorBuilder(topicName, BrokerType.Kafka, reason, grpcStatus)
    for {
      events <- request.brokerRequest match {
        case Some(meta) =>
          meta.brokerType match {
            case ProtoBrokerType.BROKER_TYPE_KAFKA =>
              ZIO
                .attempt {
                  val consumer: KafkaConsumer[String, String] = new KafkaConsumer(props)
                  val events = mutable.ListBuffer.empty[(String, String)]
                  consumer.subscribe(List(meta.topic).asJava)
                  consumer.poll(Duration.ofMillis(1000)).asScala.foreach(kv => events.addOne((kv.key(), kv.value())))
                  consumer.unsubscribe()
                  events.toSeq
                }
                .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
                .orElseFail(
                  errorBuilder(
                    meta.topic,
                    BrokerType.Kafka,
                    s"Could not read from kafka topic ${meta.topic}",
                    Status.INTERNAL
                  )
                )

            case ProtoBrokerType.BROKER_TYPE_RABBITMQ =>
              ZIO.fail(
                errorBuilder(meta.topic, BrokerType.RabbitMQ, s"RabbitMq is not supported yet", Status.UNIMPLEMENTED)
              )
            case ProtoBrokerType.BROKER_TYPE_UNDEFINED =>
              ZIO.fail(kafkaErrorBuilder(meta.topic, s"Broker type undefined", Status.INVALID_ARGUMENT))
            case ProtoBrokerType.Unrecognized(unrecognizedValue) =>
              ZIO.fail(
                kafkaErrorBuilder(meta.topic, s"Unrecognized broker type: $unrecognizedValue", Status.INVALID_ARGUMENT)
              )
          }
        case None => ZIO.fail(kafkaErrorBuilder("UNKNOWN", "Missing Payload", Status.INVALID_ARGUMENT))
      }

      response = GetMessagesResponse(
        messages = events
          .map(
            kv =>
              KafkaContainer(
                content = Some(KafkaEvent(key = kv._1, value = KafkaEvent.Value.StringValue(value = kv._2)))
              )
          )
          .map(
            kc => BrokerInfoContainer(container = Container.KafkaContainer(kc))
          )
      )
    } yield response
  }

  def getTopics(request: GetTopicsRequest): IO[BrokerManagerException, GetTopicsResponse] =
    for {
      topics <- request.brokerType match {
        case ProtoBrokerType.BROKER_TYPE_RABBITMQ =>
          ZIO.fail(
            BrokerManagerException.couldNotGetTopicsList(
              BrokerType.RabbitMQ,
              "RabbitMQ support not implemented",
              Status.UNIMPLEMENTED
            )
          )
        case ProtoBrokerType.Unrecognized(unrecognizedValue) =>
          ZIO.fail(
            BrokerManagerException.couldNotGetTopicsList(
              BrokerType.RabbitMQ,
              s"Unrecognized broker type: $unrecognizedValue",
              Status.INVALID_ARGUMENT
            )
          )
        case _ =>
          kafkaController.adminClient.listTopics().mapError { error =>
            BrokerManagerException.couldNotGetTopicsList(
              BrokerType.Kafka,
              s"List topics request failed due to: ${error.getStackTrace.mkString("\n")}",
              Status.INTERNAL
            )
          }
      }
    } yield GetTopicsResponse(queues = topics.values.map(t => Queue(ProtoBrokerType.BROKER_TYPE_KAFKA, t.name)).toSeq)

  def deleteTopic(request: DeleteTopicRequest): IO[BrokerManagerException, DeleteTopicResponse] = {
    for {
      _ <- request.brokerType match {
        case ProtoBrokerType.BROKER_TYPE_KAFKA =>
          kafkaController.adminClient
            .deleteTopic(request.topicName)
            .orElseFail(
              BrokerManagerException.couldNotDeleteTopic(
                request.topicName,
                BrokerType.Kafka,
                "Reason unknown",
                Status.INTERNAL
              )
            )
        case ProtoBrokerType.BROKER_TYPE_RABBITMQ =>
          ZIO.fail(
            BrokerManagerException.couldNotDeleteTopic(
              request.topicName,
              BrokerType.Unknown,
              s"RabbitMQ not supported yet",
              Status.UNIMPLEMENTED
            )
          )
        case ProtoBrokerType.Unrecognized(unrecognizedValue) =>
          ZIO.fail(
            BrokerManagerException.couldNotDeleteTopic(
              request.topicName,
              BrokerType.Unknown,
              s"Unrecognized broker type $unrecognizedValue",
              Status.INVALID_ARGUMENT
            )
          )
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
    } yield DeleteTopicResponse(success = true)
  }

  private def newTopic(topicName: String): UIO[AdminClient.NewTopic] =
    ZIO.succeed(AdminClient.NewTopic(topicName, 1, 1))

  private def createKafkaTopic(request: CreateTopicRequest): IO[BrokerManagerException, CreateTopicResponse] = {
    def topicCreationException(
        reason: String = "reason unknown",
        status: Status = Status.INTERNAL
    ): BrokerManagerException =
      BrokerManagerException.couldNotCreateTopic(request.topicName, BrokerType.Kafka, reason, status)
    for {
      newTopic <- newTopic(request.topicName)
      _ <- kafkaController.adminClient.createTopic(newTopic).orElseFail(topicCreationException())
      topics <- kafkaController.adminClient.listTopics().orElseSucceed(Map.empty)
      response <- ZIO.ifZIO(ZIO.succeed(topics.keySet.contains(request.topicName)))(
        onTrue = ZIO.succeed(
          CreateTopicResponse(
            host = kafkaController.address.host,
            port = kafkaController.address.port,
            topicName = request.topicName
          )
        ),
        onFalse = ZIO.fail(topicCreationException())
      )
    } yield response
  }
  private def sendKafkaEvent(
      container: KafkaContainer,
      errorBuilder: (String, String, Status) => BrokerManagerException,
      repeat: Int = 1
  ): IO[BrokerManagerException, RecordMetadata] = {
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
                errorBuilder(container.topic, error.getMessage, Status.INTERNAL)
              }
              .repeatN(repeat)
          case Value.Empty => ZIO.fail(errorBuilder(container.topic, "Missing Value", Status.INVALID_ARGUMENT))
        }
      case None => ZIO.fail(errorBuilder(container.topic, "Missing Content", Status.INVALID_ARGUMENT))
    }
  }
}

object DefaultMqManager {

  def layer: ZLayer[AdminClient with Producer with ServerAddress, Nothing, DefaultMqManager] = {
    ZLayer.fromZIO {
      for {
        adminClient <- ZIO.service[AdminClient]
        producer <- ZIO.service[Producer]
        brokerAddress <- ZIO.service[ServerAddress]
        kafkaController = KafkaController(adminClient, producer, brokerAddress)
      } yield DefaultMqManager(kafkaController)
    }
  }
}
