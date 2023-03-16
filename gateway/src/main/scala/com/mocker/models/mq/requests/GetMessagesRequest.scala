package com.mocker.models.mq.requests

import com.mocker.models.mq.ScalaBrokerType
import com.mocker.mq.mq_service.GetMessagesRequest.ValueParseType
import com.mocker.mq.mq_service.{BrokerRequestContainer, GetMessagesRequest => ProtoGetMessagesRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetMessagesRequest(brokerType: String, topicName: String, repeat: Int = 5) {

  def toMessage: Either[String, ProtoGetMessagesRequest] = {
    ScalaBrokerType.getBrokerType(brokerType) match {
      case Right(bt) =>
        val brokerRequestContainer = BrokerRequestContainer(
          brokerType = bt,
          topic = topicName
        )
        Right(
          ProtoGetMessagesRequest(
            brokerRequest = Some(brokerRequestContainer),
            bytesParseType = ValueParseType.STRING
          )
        )
      case Left(error) => Left(error)
    }
  }
}

object GetMessagesRequest {
  implicit val encoder = DeriveJsonEncoder.gen[GetMessagesRequest]
  implicit val decoder = DeriveJsonDecoder.gen[GetMessagesRequest]
}
