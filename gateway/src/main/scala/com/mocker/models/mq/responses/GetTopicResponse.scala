package com.mocker.models.mq.responses

import com.mocker.models.mq.ScalaBrokerType.KAFKA
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetTopicResponse(brokerType: String, host: String, port: Int, topicName: String)

object GetTopicResponse {

  def fromMessage(): GetTopicResponse = {
    GetTopicResponse(KAFKA, "158.160.57.255", 9092, "1.1.1.1")
  }

  implicit val encoder = DeriveJsonEncoder.gen[GetTopicResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetTopicsResponse]
}
