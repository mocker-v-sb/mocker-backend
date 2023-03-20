package com.mocker.models.rest.requests.service

import com.mocker.rest.rest_service.DeleteService.{Request => ProtoDeleteServiceRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class DeleteServiceRequest(servicePath: String) {

  def toMessage: ProtoDeleteServiceRequest = {
    ProtoDeleteServiceRequest(servicePath = servicePath)
  }
}

object DeleteServiceRequest {
  implicit val encoder = DeriveJsonEncoder.gen[DeleteServiceRequest]
  implicit val decoder = DeriveJsonDecoder.gen[DeleteServiceRequest]
}
