package com.mocker.models.mq.responses

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}
import com.mocker.mq.mq_service.{SendMessageResponse => ProtoSendMessageResponse}

case class SendMessageResponse(success: Boolean)

object SendMessageResponse {
  implicit val encoder = DeriveJsonEncoder.gen[SendMessageResponse]
  implicit val decoder = DeriveJsonDecoder.gen[SendMessageResponse]

  def fromMessage(message: ProtoSendMessageResponse): SendMessageResponse = {
    SendMessageResponse(success = message.success)
  }
}
