package com.mocker.models.mq.responses

import com.mocker.models.mq.ScalaBrokerType
import com.mocker.models.mq.responses.GetTopicsResponse.Queue
import com.mocker.mq.mq_service.{GetTopicsResponse => ProtoGetTopicsResponse, Queue => ProtoQueue}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetTopicsResponse(queues: Seq[Queue])

object GetTopicsResponse {
  case class Queue(brokerType: String, name: String)

  object Queue {

    def fromMessage(message: ProtoQueue): Either[String, Queue] = {
      ScalaBrokerType.fromMessage(message.brokerType) match {
        case Right(sbt)  => Right(Queue(brokerType = sbt, name = message.topicName))
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
