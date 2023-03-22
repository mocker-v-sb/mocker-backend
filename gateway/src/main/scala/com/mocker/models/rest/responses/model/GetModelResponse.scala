package com.mocker.models.rest.responses.model

import com.mocker.rest.rest_service.GetModel.{Response => ProtoGetModelResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetModelResponse(
    modelId: Long,
    name: String,
    description: Option[String],
    sample: String
)

object GetModelResponse {
  implicit val encoder = DeriveJsonEncoder.gen[GetModelResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetModelResponse]

  def fromMessage(message: ProtoGetModelResponse): GetModelResponse = {
    GetModelResponse(
      modelId = message.id,
      name = message.name,
      description = message.description,
      sample = message.sample
    )
  }
}
