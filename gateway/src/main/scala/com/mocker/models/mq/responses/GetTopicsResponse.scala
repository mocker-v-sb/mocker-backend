package com.mocker.models.mq.responses

import com.mocker.models.mq.ScalaBrokerType
import com.mocker.models.mq.responses.GetTopicsResponse.Queue
import com.mocker.mq.mq_service.{GetTopicsResponse => ProtoGetTopicsResponse, Queue => ProtoQueue}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder}

case class GetTopicsResponse(queues: Seq[Queue])

// todo: ???
case class Helper(name: String, value: String)

object Helper {
  implicit val decoder: JsonDecoder[Helper] = DeriveJsonDecoder.gen[Helper]
}

object GetTopicsResponse {
  case class Queue(brokerType: String, topicName: String)

  object Queue {

    def fromMessage(message: ProtoQueue): Either[String, Queue] = {
      ScalaBrokerType.fromMessage(message.brokerType) match {
        case Right(sbt)  => Right(Queue(brokerType = sbt, topicName = message.topicName))
        case Left(error) => Left(error)
      }
    }

    implicit val encoder = DeriveJsonEncoder.gen[Queue]
    implicit val decoder = DeriveJsonDecoder.gen[Queue]
  }

  def fromMessage(message: ProtoGetTopicsResponse): Either[String, GetTopicsResponse] = {
    val queuesAndErrors = message.queues.map(q => Queue.fromMessage(q))
    val errors = queuesAndErrors.collect { case Left(error)  => error }
    val queues = queuesAndErrors.collect { case Right(queue) => queue }
    if (errors.isEmpty) Right(GetTopicsResponse(queues))
    else Left(errors.head)
  }

  implicit val encoder = DeriveJsonEncoder.gen[GetTopicsResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetTopicsResponse]
}
