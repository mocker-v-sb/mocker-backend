package com.mocker.models.rest.requests.model

import com.mocker.rest.rest_service.GetAllServiceModels.{Request => ProtoGetAllServiceModelsRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetAllServiceModelsRequest(servicePath: String) {

  def toMessage: ProtoGetAllServiceModelsRequest = {
    ProtoGetAllServiceModelsRequest(servicePath = servicePath)
  }
}

object GetAllServiceModelsRequest {
  implicit val encoder = DeriveJsonEncoder.gen[GetAllServiceModelsRequest]
  implicit val decoder = DeriveJsonDecoder.gen[GetAllServiceModelsRequest]
}
