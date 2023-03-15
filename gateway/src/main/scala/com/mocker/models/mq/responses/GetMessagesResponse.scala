package com.mocker.models.mq.responses


import GetMessagesResponse.Event
import com.mocker.mq.mq_service.KafkaEvent.Value
import com.mocker.mq.mq_service.{KafkaContainer, GetMessagesResponse => ProtoGetMessagesResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetMessagesResponse(messages: Seq[Event])

object GetMessagesResponse {
  case class Event(topic: String, key: String, content: String)

  object Event {
    def fromMessage(kafkaContainer: KafkaContainer): Either[String, Event] = {
      kafkaContainer.content.map(ke => Event(topic = kafkaContainer.topic, key = ke.key, content = ke.value match {
        case Value.Empty => "NAN"
        case Value.StringValue(str) => str
      }))
    }.toRight(s"Could not parse kafka event response:\n${kafkaContainer.toProtoString}")

    implicit val encoder = DeriveJsonEncoder.gen[Event]
    implicit val decoder = DeriveJsonDecoder.gen[Event]
  }

  def fromMessage(message: ProtoGetMessagesResponse): Either[String, GetMessagesResponse] = {
    val errorsAndEvents: Seq[Either[String, Event]] =
      message.messages.flatMap(container => container.container.kafkaContainer.map(Event.fromMessage))
    val errors = errorsAndEvents.collect { case Left(error) => error }
    val events = errorsAndEvents.collect { case Right(event) => event }
    if (errors.isEmpty) Right(GetMessagesResponse(events)) else Left(errors.head)
  }

  implicit val encoder = DeriveJsonEncoder.gen[GetMessagesResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetMessagesResponse]
}
