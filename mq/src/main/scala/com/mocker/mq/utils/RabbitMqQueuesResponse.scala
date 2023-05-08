package com.mocker.mq.utils

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class RabbitMqQueuesResponse(name: String) // ignoring all other fields

object RabbitMqQueuesResponse {
  implicit val encoder = DeriveJsonEncoder.gen[RabbitMqQueuesResponse]
  implicit val decoder = DeriveJsonDecoder.gen[RabbitMqQueuesResponse]
}
