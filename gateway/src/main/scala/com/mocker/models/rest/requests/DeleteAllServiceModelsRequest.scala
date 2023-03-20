package com.mocker.models.rest.requests

import com.mocker.rest.rest_service.DeleteAllModels.{Request => ProtoDeleteAllModelsRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class DeleteAllServiceModelsRequest(servicePath: String) {

  def toMessage: ProtoDeleteAllModelsRequest = {
    ProtoDeleteAllModelsRequest(servicePath = servicePath)
  }
}

object DeleteAllServiceModelsRequest {
  implicit val encoder = DeriveJsonEncoder.gen[DeleteAllServiceModelsRequest]
  implicit val decoder = DeriveJsonDecoder.gen[DeleteAllServiceModelsRequest]
}
