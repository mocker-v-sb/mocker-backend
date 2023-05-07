package com.mocker.models.mq.requests

import com.mocker.models.mq.ScalaBrokerType
import com.mocker.mq.mq_service.{
  MessagesContainer,
  BrokerType => ProtoBrokerType,
  SendMessageRequest => ProtoSendMessageRequest
}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class SendMessageRequest(brokerType: String, topicName: String, key: String, content: String, repeat: Int) {

  def toMessage: Either[String, ProtoSendMessageRequest] = {
    ScalaBrokerType.getBrokerType(brokerType) match {
      case Right(bt) =>
        bt match {
          case ProtoBrokerType.BROKER_TYPE_KAFKA | ProtoBrokerType.BROKER_TYPE_RABBITMQ =>
            val messagesContainer = MessagesContainer(topicName, key, content)
            Right(ProtoSendMessageRequest(messagesContainer = Some(messagesContainer), repeat = repeat))
          case _                                    => Left("Broker type not defined")
        }
      case Left(error) => Left(error)
    }
  }
}

object SendMessageRequest {
  implicit val encoder = DeriveJsonEncoder.gen[SendMessageRequest]
  implicit val decoder = DeriveJsonDecoder.gen[SendMessageRequest]
}
