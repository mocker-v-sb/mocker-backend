package com.mocker.models.rest.requests.mock

import com.mocker.rest.rest_service.DeleteAllMocks.{Request => ProtoDeleteAllMocksRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class DeleteAllServiceMocksRequest(servicePath: String) {

  def toMessage: ProtoDeleteAllMocksRequest = {
    ProtoDeleteAllMocksRequest(servicePath = servicePath)
  }
}

object DeleteAllServiceMocksRequest {
  implicit val encoder = DeriveJsonEncoder.gen[DeleteAllServiceMocksRequest]
  implicit val decoder = DeriveJsonDecoder.gen[DeleteAllServiceMocksRequest]
}
