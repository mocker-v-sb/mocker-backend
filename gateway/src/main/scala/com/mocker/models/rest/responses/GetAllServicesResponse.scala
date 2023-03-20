package com.mocker.models.rest.responses

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}
import com.mocker.rest.rest_service.GetAllServices.{Response => ProtoGetAllServicesResponse}

case class GetAllServicesResponse(services: Seq[ServiceSnippet])

object GetAllServicesResponse {

  implicit val encoder = DeriveJsonEncoder.gen[GetAllServicesResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetAllServicesResponse]

  def fromMessage(message: ProtoGetAllServicesResponse): GetAllServicesResponse = {
    GetAllServicesResponse(
      message.services.map(ServiceSnippet.fromMessage)
    )
  }
}
