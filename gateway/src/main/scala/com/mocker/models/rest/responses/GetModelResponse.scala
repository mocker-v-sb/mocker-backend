package com.mocker.models.rest.responses

import com.mocker.rest.rest_service.GetModel.{Response => ProtoGetModelResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetModelResponse(
    name: String,
    description: Option[String],
    schema: String
)

object GetModelResponse {
  implicit val encoder = DeriveJsonEncoder.gen[GetModelResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetModelResponse]

  def fromMessage(message: ProtoGetModelResponse): GetModelResponse = {
    GetModelResponse(
      name = message.name,
      description = message.description,
      schema = message.schema
    )
  }
}
