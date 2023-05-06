package com.mocker.mq.adapters

import com.mocker.common.utils.ServerAddress
import com.mocker.mq.mq_service.{BrokerType => ProtoBrokerType, _}
import com.mocker.mq.ports.MqController
import com.mocker.mq.utils.{BrokerManagerException, BrokerType}
import io.grpc.Status
import org.apache.kafka.clients.consumer.KafkaConsumer
import zio.kafka.admin.AdminClient
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.{Cause, IO, ZIO}

import java.time.Duration
import java.util.Properties
import scala.collection.mutable
import scala.jdk.CollectionConverters._

case class KafkaController(adminClient: AdminClient, producer: Producer, address: ServerAddress) extends MqController {

  import KafkaController._

  def createQueue(request: CreateTopicRequest): IO[BrokerManagerException, CreateTopicResponse] = {
    def topicCreationException(
        reason: String = "reason unknown",
        status: Status = Status.INTERNAL
    ): BrokerManagerException =
      BrokerManagerException.couldNotCreateTopic(request.topicName, BrokerType.Kafka, reason, status)

    for {
      newTopic <- ZIO.succeed(AdminClient.NewTopic(request.topicName, 1, 1))
      _ <- adminClient.createTopic(newTopic).orElseFail(topicCreationException())
      topics <- adminClient.listTopics().orElseSucceed(Map.empty)
      response <- ZIO.ifZIO(ZIO.succeed(topics.keySet.contains(request.topicName)))(
        onTrue = ZIO.succeed(
          CreateTopicResponse(
            host = address.host,
            port = address.port,
            topicName = request.topicName
          )
        ),
        onFalse = ZIO.fail(topicCreationException())
      )
    } yield response
  }

  override def deleteTopic(request: DeleteTopicRequest): IO[BrokerManagerException, DeleteTopicResponse] =
    adminClient
      .deleteTopic(request.topicName)
      .mapBoth(
        _ =>
          BrokerManagerException.couldNotDeleteTopic(
            request.topicName,
            BrokerType.Kafka,
            "Reason unknown",
            Status.INTERNAL
          ),
        _ => DeleteTopicResponse(success = true)
      )

  override def sendMessages(request: SendMessageRequest): IO[BrokerManagerException, SendMessageResponse] = {
    request.messagesContainer match {
      case Some(messagesContainer) =>
        producer
          .produce[Any, String, String](
            topic = messagesContainer.queue,
            key = messagesContainer.key,
            value = messagesContainer.value,
            keySerializer = Serde.string,
            valueSerializer = Serde.string
          )
          .mapError { error =>
            eventSendErrorBuilder(messagesContainer.queue, BrokerType.Kafka, error.getMessage, Status.INTERNAL)
          }
          .repeatN(request.repeat - 1)
          .as(SendMessageResponse(success = true))
      case None =>
        ZIO.fail(eventSendErrorBuilder("UNKNOWN", BrokerType.Unknown, "Missing Content", Status.INVALID_ARGUMENT))
    }
  }

  private val props: Properties = new Properties()
  props.put("bootstrap.servers", s"$address")
  props.put("group.id", "kafka-mocker")
  props.put("enable.auto.commit", "true")
  props.put("auto.commit.interval.ms", "1000")
  props.put("session.timeout.ms", "30000")
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

  override def getMessages(request: GetMessagesRequest): IO[BrokerManagerException, GetMessagesResponse] =
    ZIO
      .attempt {
        val consumer: KafkaConsumer[String, String] = new KafkaConsumer(props)
        val events = mutable.ListBuffer.empty[MessagesContainer]
        consumer.subscribe(List(request.topic).asJava)
        consumer
          .poll(Duration.ofMillis(1000))
          .asScala
          .foreach(
            kv =>
              events.addOne(
                MessagesContainer(
                  queue = request.topic,
                  key = kv.key(),
                  value = kv.value()
                )
              )
          )
        consumer.unsubscribe()
        events.toSeq
      }
      .tapError(err => ZIO.logErrorCause(Cause.fail(err)))
      .mapBoth(
        _ =>
          BrokerManagerException.couldNotReadFromTopic(
            request.topic,
            BrokerType.Kafka,
            s"Could not read from kafka topic ${request.topic}",
            Status.INTERNAL
          ),
        events => GetMessagesResponse(ProtoBrokerType.BROKER_TYPE_KAFKA, events)
      )

  override def getQueues(request: GetTopicsRequest): IO[BrokerManagerException, GetTopicsResponse] =
    adminClient
      .listTopics()
      .mapBoth(
        error =>
          BrokerManagerException.couldNotGetTopicsList(
            BrokerType.Kafka,
            s"List topics request failed due to: ${error.getStackTrace.mkString("\n")}",
            Status.INTERNAL
          ),
        qs => GetTopicsResponse(qs.valuesIterator.map(q => Queue(ProtoBrokerType.BROKER_TYPE_KAFKA, q.name)).toSeq)
      )
}

object KafkaController {

  def eventSendErrorBuilder(
      topicName: String,
      brokerType: BrokerType = BrokerType.Unknown,
      reason: String,
      grpcStatus: Status
  ): BrokerManagerException =
    BrokerManagerException.couldNotSendEvent(topicName, brokerType, reason, grpcStatus)
}
