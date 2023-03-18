package com.mocker.models.mq.requests

import com.mocker.models.mq.ScalaBrokerType
import com.mocker.mq.mq_service.{DeleteTopicRequest => ProtoDeleteTopicRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class DeleteTopicRequest(brokerType: String, topicName: String) {

  def toMessage: Either[String, ProtoDeleteTopicRequest] = {
    ScalaBrokerType.getBrokerType(brokerType) match {
      case Right(value) => Right(ProtoDeleteTopicRequest(brokerType = value, topicName = topicName))
      case Left(value)  => Left(value)
    }
  }
}

object DeleteTopicRequest {
  implicit val encoder = DeriveJsonEncoder.gen[DeleteTopicRequest]
  implicit val decoder = DeriveJsonDecoder.gen[DeleteTopicRequest]
}
