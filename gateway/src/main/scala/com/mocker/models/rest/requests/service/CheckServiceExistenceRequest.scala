package com.mocker.models.rest.requests.service

import com.mocker.rest.rest_service.CheckServiceExistence.{Request => ProtoCheckServiceExistenceRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class CheckServiceExistenceRequest(servicePath: String) {

  def toMessage: ProtoCheckServiceExistenceRequest = {
    ProtoCheckServiceExistenceRequest(path = servicePath)
  }
}

object CheckServiceExistenceRequest {
  implicit val encoder = DeriveJsonEncoder.gen[CheckServiceExistenceRequest]
  implicit val decoder = DeriveJsonDecoder.gen[CheckServiceExistenceRequest]
}
