package com.mocker.models.rest.requests.model

import com.mocker.rest.rest_service.GetModel.{Request => ProtoGetModelRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetModelRequest(servicePath: String, modelId: Long) {

  def toMessage: ProtoGetModelRequest = {
    ProtoGetModelRequest(servicePath = servicePath, modelId = modelId)
  }
}

object GetModelRequest {
  implicit val encoder = DeriveJsonEncoder.gen[GetModelRequest]
  implicit val decoder = DeriveJsonDecoder.gen[GetModelRequest]
}
