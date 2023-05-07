package com.mocker.models.mq.responses

import com.mocker.models.mq.responses.GetMessagesResponse.Event
import com.mocker.mq.mq_service.{MessagesContainer, GetMessagesResponse => ProtoGetMessagesResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetMessagesResponse(messages: Seq[Event])

object GetMessagesResponse {
  case class Event(key: String, content: String)

  object Event {

    def fromMessage(messagesContainer: MessagesContainer): Event = {
      Event(messagesContainer.key, messagesContainer.value)
    }

    implicit val encoder = DeriveJsonEncoder.gen[Event]
    implicit val decoder = DeriveJsonDecoder.gen[Event]
  }

  def fromMessage(response: ProtoGetMessagesResponse): GetMessagesResponse = {
    val events: Seq[Event] = response.messages.map(Event.fromMessage)
    GetMessagesResponse(events)
  }

  implicit val encoder = DeriveJsonEncoder.gen[GetMessagesResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetMessagesResponse]
}
