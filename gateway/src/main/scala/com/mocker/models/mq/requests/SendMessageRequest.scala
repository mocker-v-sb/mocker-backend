package com.mocker.models.mq.requests

import com.mocker.models.mq.BrokerType
import com.mocker.mq.mq_service.BrokerInfoContainer.Container
import com.mocker.mq.mq_service.KafkaEvent.Value.StringValue
import com.mocker.mq.mq_service.{BrokerInfoContainer, KafkaContainer, KafkaEvent, BrokerType => ProtoBrokerType, SendMessageRequest => ProtoSendMessageRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class SendMessageRequest(brokerType: String, topic: String, key: String, content: String, repeat: Int) {

  def toMessage: Either[String, ProtoSendMessageRequest] = {
    BrokerType.getBrokerType(brokerType) match {
      case Right(bt) =>
        bt match {
          case ProtoBrokerType.BROKER_TYPE_KAFKA =>
            ???
            val event = KafkaEvent(key = key, value = StringValue(content))
            val container = BrokerInfoContainer(
              container = Container.KafkaContainer(value = KafkaContainer(topic = topic, content = Some(event)))
            )
            Right(ProtoSendMessageRequest(brokerInfoContainer = Some(container), repeat = repeat))
          case ProtoBrokerType.BROKER_TYPE_RABBITMQ  => Left("RabbitMQ support is not implemented yet.")
          case ProtoBrokerType.BROKER_TYPE_UNDEFINED => Left("Broker type not defined")
        }
      case Left(error) => Left(error)
    }
  }
}

object SendMessageRequest {
  implicit val encoder = DeriveJsonEncoder.gen[SendMessageRequest]
  implicit val decoder = DeriveJsonDecoder.gen[SendMessageRequest]
}
