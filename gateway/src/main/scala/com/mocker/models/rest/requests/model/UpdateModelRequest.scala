package com.mocker.models.rest.requests.model

import com.mocker.rest.rest_service.UpdateModel.{Request => ProtoUpdateModelRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class UpdateModelRequest(
    servicePath: String = "",
    modelId: Long = 0,
    name: String,
    description: Option[String],
    sample: String
) {

  def toMessage: ProtoUpdateModelRequest = {
    ProtoUpdateModelRequest(
      servicePath = servicePath,
      modelId = modelId,
      name = name,
      description = description,
      sample = sample
    )
  }
}

object UpdateModelRequest {
  implicit val encoder = DeriveJsonEncoder.gen[UpdateModelRequest]
  implicit val decoder = DeriveJsonDecoder.gen[UpdateModelRequest]
}
