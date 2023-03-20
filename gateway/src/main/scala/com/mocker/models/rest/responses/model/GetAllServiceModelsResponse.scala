package com.mocker.models.rest.responses.model

import com.mocker.rest.rest_service.GetAllServiceModels.{Response => ProtoGetAllServiceModelsResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetAllServiceModelsResponse(models: Seq[ModelSnippet])

object GetAllServiceModelsResponse {

  implicit val encoder = DeriveJsonEncoder.gen[GetAllServiceModelsResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetAllServiceModelsResponse]

  def fromMessage(message: ProtoGetAllServiceModelsResponse): GetAllServiceModelsResponse = {
    GetAllServiceModelsResponse(
      message.models.map(ModelSnippet.fromMessage)
    )
  }
}
