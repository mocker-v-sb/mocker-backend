package com.mocker.models.rest.responses

import com.mocker.rest.service.{ServiceSnippet => ProtoServiceSnippet}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class ServiceSnippet(name: String, path: String, url: Option[String], totalMocks: Long, totalModels: Long)

object ServiceSnippet {
  implicit val encoder = DeriveJsonEncoder.gen[ServiceSnippet]
  implicit val decoder = DeriveJsonDecoder.gen[ServiceSnippet]

  def fromMessage(message: ProtoServiceSnippet): ServiceSnippet = {
    ServiceSnippet(
      name = message.name,
      path = message.path,
      url = message.url,
      totalMocks = message.totalMocks,
      totalModels = message.totalModels
    )
  }
}
