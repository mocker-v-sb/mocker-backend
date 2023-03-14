package com.mocker.models.mq.requests

import com.mocker.models.mq.BrokerType
import com.mocker.mq.mq_service.{CreateTopicRequest => ProtoCreateTopicRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class CreateTopicRequest(brokerType: String, topicName: String) {

  def toProto: Either[String, ProtoCreateTopicRequest] = {
    BrokerType.getBrokerType(brokerType) match {
      case Right(value) => Right(ProtoCreateTopicRequest(brokerType = value, topicName = topicName))
      case Left(value)  => Left(value)
    }
  }
}

object CreateTopicRequest {
  implicit val encoder = DeriveJsonEncoder.gen[CreateTopicRequest]
  implicit val decoder = DeriveJsonDecoder.gen[CreateTopicRequest]
}
