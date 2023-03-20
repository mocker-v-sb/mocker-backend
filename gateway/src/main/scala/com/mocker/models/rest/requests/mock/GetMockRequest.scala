package com.mocker.models.rest.requests.mock

import com.mocker.rest.rest_service.GetMock.{Request => ProtoGetMockRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetMockRequest(servicePath: String, mockId: Long) {

  def toMessage: ProtoGetMockRequest = {
    ProtoGetMockRequest(servicePath = servicePath, mockId = mockId)
  }
}

object GetMockRequest {
  implicit val encoder = DeriveJsonEncoder.gen[GetMockRequest]
  implicit val decoder = DeriveJsonDecoder.gen[GetMockRequest]
}
