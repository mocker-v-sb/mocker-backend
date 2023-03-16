package com.mocker.models.mq.responses

import com.mocker.mq.mq_service.{CreateTopicResponse => ProtoCreateTopicResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class CreateTopicResponse(brokerType: String, host: String, port: Int, topicName: String)

object CreateTopicResponse {
  implicit val encoder = DeriveJsonEncoder.gen[CreateTopicResponse]
  implicit val decoder = DeriveJsonDecoder.gen[CreateTopicResponse]

  def fromMessage(message: ProtoCreateTopicResponse): CreateTopicResponse = {
    CreateTopicResponse(
      brokerType = message.brokerType.toString(),
      host = message.host,
      port = message.port,
      topicName = message.topicName
    )
  }
}
