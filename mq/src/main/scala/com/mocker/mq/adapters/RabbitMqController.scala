package com.mocker.mq.adapters

import com.mocker.common.utils.ServerAddress
import com.mocker.mq.mq_service.{BrokerType => ProtoBrokerType, _}
import com.mocker.mq.ports.MqController
import com.mocker.mq.utils.{BrokerManagerException, BrokerType, RabbitMqQueuesResponse}
import com.rabbitmq.client.Channel
import io.grpc.Status
import zio.http.URL.Location
import zio.http.{Client, Path, Request, URL}
import zio.http.model.{Header, Method, Scheme}
import zio.json.DecoderOps
import zio.{IO, ZIO, ZLayer}

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks._

case class RabbitMqController(channel: Channel, address: ServerAddress, httpClient: Client) extends MqController {
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
            brokerType = ProtoBrokerType.BROKER_TYPE_RABBITMQ,
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

  private val rabbitServerAuthCreds = Base64.getEncoder.encodeToString("guest:guest".getBytes)
  override def getQueues(request: GetTopicsRequest): IO[BrokerManagerException, GetTopicsResponse] =
    for {
      url <- ZIO.succeed(
        URL(
          kind = Location.Absolute(
            scheme = Scheme.HTTP,
            host = s"localhost",
            port = 15672
          ),
          path = Path.decode("/api/queues")
        )
      )
      fullResponse <- httpClient
        .request(
          Request.get(url = url).updateHeaders(_.combine(Header("Authorization", s"Basic $rabbitServerAuthCreds")))
        )
        .tapError { error =>
          ZIO.logError(error.getStackTrace.mkString("\n\t"))
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
        _.fromJson[Seq[RabbitMqQueuesResponse]]
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
        case Right(qs) =>
          ZIO.succeed(GetTopicsResponse(qs.map(q => Queue(ProtoBrokerType.BROKER_TYPE_RABBITMQ, q.name))))
      }
    } yield response

  override def sendMessages(request: SendMessageRequest): IO[BrokerManagerException, SendMessageResponse] =
    request.messagesContainer match {
      case Some(messagesContainer) =>
        for {
          _ <- ZIO
            .attempt(channel.queueDeclare(messagesContainer.queue, false, false, false, null))
            .mapError { error =>
              BrokerManagerException.couldNotSendEvent(
                messagesContainer.queue,
                BrokerType.RabbitMQ,
                s"Failed to recreate queue due to: ${error.getMessage}\n${error.getStackTrace.mkString("\n\t")}",
                Status.NOT_FOUND
              )
            }
          response <- ZIO
            .attempt {
              channel.basicPublish(
                "",
                messagesContainer.queue,
                null,
                messagesContainer.value.getBytes(StandardCharsets.UTF_8)
              )
            }
            .repeatN(request.repeat - 1)
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
        } yield response
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
        val message = channel.basicGet(request.topic, true)
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

object RabbitMqController {

  def live = ZLayer.fromZIO {
    for {
      channel <- ZIO.service[Channel]
      brokerAddress <- ZIO.service[ServerAddress]
      httpClient <- ZIO.service[Client]
    } yield RabbitMqController(channel, brokerAddress, httpClient)
  }
}
