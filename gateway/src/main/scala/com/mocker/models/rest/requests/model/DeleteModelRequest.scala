package com.mocker.models.rest.requests.model

import com.mocker.rest.rest_service.DeleteModel.{Request => ProtoDeleteModelRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class DeleteModelRequest(servicePath: String, modelId: Long) {

  def toMessage: ProtoDeleteModelRequest = {
    ProtoDeleteModelRequest(servicePath = servicePath, modelId = modelId)
  }
}

object DeleteModelRequest {
  implicit val encoder = DeriveJsonEncoder.gen[DeleteModelRequest]
  implicit val decoder = DeriveJsonDecoder.gen[DeleteModelRequest]
}
