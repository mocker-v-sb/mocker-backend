package com.mocker.models.rest.requests.mock

import com.mocker.rest.rest_service.GetAllServiceMocks.{Request => ProtoGetAllServiceMocksRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetAllServiceMocksRequest(servicePath: String) {

  def toMessage: ProtoGetAllServiceMocksRequest = {
    ProtoGetAllServiceMocksRequest(servicePath = servicePath)
  }
}

object GetAllServiceMocksRequest {
  implicit val encoder = DeriveJsonEncoder.gen[GetAllServiceMocksRequest]
  implicit val decoder = DeriveJsonDecoder.gen[GetAllServiceMocksRequest]
}
