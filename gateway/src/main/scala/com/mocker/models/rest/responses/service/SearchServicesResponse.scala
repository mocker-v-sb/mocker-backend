package com.mocker.models.rest.responses.service

import com.mocker.rest.rest_service.SearchServices.{Response => ProtoSearchServicesResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class SearchServicesResponse(services: Seq[ServiceSnippet])

object SearchServicesResponse {

  implicit val encoder = DeriveJsonEncoder.gen[SearchServicesResponse]
  implicit val decoder = DeriveJsonDecoder.gen[SearchServicesResponse]

  def fromMessage(message: ProtoSearchServicesResponse): SearchServicesResponse = {
    SearchServicesResponse(
      message.services.map(ServiceSnippet.fromMessage)
    )
  }
}
