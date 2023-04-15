package com.mocker.models.rest.requests.model

import com.mocker.models.rest.common.ModelResponseType
import com.mocker.rest.rest_service.UpdateModel.{Request => ProtoUpdateModelRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class UpdateModelRequest(
    servicePath: String = "",
    modelId: Long = 0,
    name: String,
    description: Option[String],
    responseType: ModelResponseType,
    response: String
) {

  def toMessage: ProtoUpdateModelRequest = {
    ProtoUpdateModelRequest(
      servicePath = servicePath,
      modelId = modelId,
      name = name,
      description = description,
      responseType = responseType.proto,
      response = response
    )
  }
}

object UpdateModelRequest {
  implicit val encoder = DeriveJsonEncoder.gen[UpdateModelRequest]
  implicit val decoder = DeriveJsonDecoder.gen[UpdateModelRequest]
}
