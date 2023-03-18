package com.mocker.models.mq.responses

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}
import com.mocker.mq.mq_service.{DeleteTopicResponse => ProtoDeleteTopicResponse}

case class DeleteTopicResponse(success: Boolean)

object DeleteTopicResponse {
  implicit val encoder = DeriveJsonEncoder.gen[DeleteTopicResponse]
  implicit val decoder = DeriveJsonDecoder.gen[DeleteTopicResponse]

  def fromMessage(message: ProtoDeleteTopicResponse): DeleteTopicResponse = {
    DeleteTopicResponse(success = message.success)
  }
}
