package com.mocker.models.mq

import com.mocker.models.mq.ScalaBrokerType
import com.mocker.mq.mq_service.{GetTopicsResponse => ProtoGetTopicsResponse, Queue => ProtoQueue}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder}

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
