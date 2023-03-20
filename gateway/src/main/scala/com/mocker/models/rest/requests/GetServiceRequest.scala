package com.mocker.models.rest.requests

import com.mocker.rest.rest_service.GetService.{Request => ProtoGetServiceRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetServiceRequest(servicePath: String) {

  def toMessage: ProtoGetServiceRequest = {
    ProtoGetServiceRequest(path = servicePath)
  }
}

object GetServiceRequest {
  implicit val encoder = DeriveJsonEncoder.gen[GetServiceRequest]
  implicit val decoder = DeriveJsonDecoder.gen[GetServiceRequest]
}
