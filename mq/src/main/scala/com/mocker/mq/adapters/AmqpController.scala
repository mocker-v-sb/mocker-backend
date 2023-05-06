package com.mocker.mq.adapters

import com.mocker.common.utils.ServerAddress
import com.mocker.mq.mq_service.{BrokerType => ProtoBrokerType, _}
import com.mocker.mq.ports.MqController
import com.mocker.mq.utils.{BrokerManagerException, BrokerType}
import com.rabbitmq.client.Channel
import io.grpc.Status
import zio.http.URL.Location
import zio.http.model.{Method, Scheme}
import zio.http.{Client, Path, Request, URL}
import zio.json.DecoderOps
import zio.{IO, ZIO, ZLayer}

import java.nio.charset.StandardCharsets
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks._

case class AmqpController(channel: Channel, address: ServerAddress, httpClient: Client) extends MqController {
  override def createQueue(request: CreateTopicRequest): IO[BrokerManagerException, CreateTopicResponse] =
    ZIO
      .attempt(channel.queueDeclare(request.topicName, false, false, false, null))
      .mapBoth(
        _ =>
          BrokerManagerException.couldNotCreateTopic(
            request.topicName,
            BrokerType.Kafka,
            "reason unknown",
            Status.INTERNAL
          ),
        _ =>
          CreateTopicResponse(
            host = address.host,
            port = address.port,
            topicName = request.topicName
          )
      )

  override def deleteQueue(request: DeleteTopicRequest): IO[BrokerManagerException, DeleteTopicResponse] =
    ZIO
      .attempt(channel.queueDelete(request.topicName))
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

  override def getQueues(request: GetTopicsRequest): IO[BrokerManagerException, GetTopicsResponse] =
    for {
      url <- ZIO.succeed(
        URL(
          kind = Location.Absolute(
            scheme = Scheme.HTTP,
            host = address.host,
            port = address.port
          ),
          path = Path.decode("/api/queues")
        )
      )
      fullResponse <- httpClient
        .request(
          Request.default(
            method = Method.GET,
            url = url
          )
        )
        .tapError { error =>
          ZIO.logError(error.getStackTrace.mkString("\n"))
        }
        .mapError { error =>
          {
            BrokerManagerException.couldNotGetTopicsList(
              BrokerType.Kafka,
              s"List topics request failed due to: ${error.getStackTrace.mkString("\n")}",
              Status.INTERNAL
            )
          }
        }
      rawResponse <- fullResponse.body.asString.mapBoth(
        error =>
          BrokerManagerException.couldNotGetTopicsList(
            BrokerType.Kafka,
            s"Failed to parse topics list due to: ${error.getMessage},\nraw response: ${fullResponse.body.asString}",
            Status.INTERNAL
          ),
        _.fromJson[Seq[String]]
      )
      response <- rawResponse match {
        case Left(error) =>
          ZIO.fail(
            BrokerManagerException.couldNotGetTopicsList(
              BrokerType.Kafka,
              s"Failed to parse topics list due to: $error,\nraw response: ${fullResponse.body.asString}",
              Status.INTERNAL
            )
          )
        case Right(qs) => ZIO.succeed(GetTopicsResponse(qs.map(q => Queue(ProtoBrokerType.BROKER_TYPE_KAFKA, q))))
      }
    } yield response

  override def sendMessages(request: SendMessageRequest): IO[BrokerManagerException, SendMessageResponse] =
    request.messagesContainer match {
      case Some(messagesContainer) =>
        ZIO
          .attempt {
            channel.basicPublish(
              "",
              messagesContainer.queue,
              null,
              messagesContainer.value.getBytes(StandardCharsets.UTF_8)
            )
          }
          .mapBoth(
            error =>
              BrokerManagerException.couldNotSendEvent(
                messagesContainer.queue,
                BrokerType.RabbitMQ,
                s"Encountered error while publishing: ${error.getMessage}",
                Status.INVALID_ARGUMENT
              ),
            _ => SendMessageResponse(success = true)
          )
      case None =>
        ZIO.fail(
          BrokerManagerException.couldNotSendEvent(
            "UNKNOWN",
            BrokerType.RabbitMQ,
            "Missing messages container",
            Status.INVALID_ARGUMENT
          )
        )
    }

  override def getMessages(request: GetMessagesRequest): IO[BrokerManagerException, GetMessagesResponse] = {
    val messages = ListBuffer.empty[String]
    breakable {
      while (true) {
        val message = channel.basicGet(request.topic, false)
        if (message == null) break
        messages += new String(message.getBody, "UTF-8")
      }
    }
    ZIO.succeed(
      GetMessagesResponse(
        ProtoBrokerType.BROKER_TYPE_RABBITMQ,
        messages.map(m => MessagesContainer(queue = request.topic, value = m)).toSeq
      )
    )
  }
}

object AmqpController {

  def live = ZLayer.fromZIO {
    for {
      channel <- ZIO.service[Channel]
      brokerAddress <- ZIO.service[ServerAddress]
      httpClient <- ZIO.service[Client]
    } yield AmqpController(channel, brokerAddress, httpClient)
  }
}
