package com.mocker.models.rest.requests.mock

import com.mocker.rest.rest_service.DeleteMock.{Request => ProtoDeleteMockRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class DeleteMockRequest(servicePath: String, mockId: Long) {

  def toMessage: ProtoDeleteMockRequest = {
    ProtoDeleteMockRequest(servicePath = servicePath, mockId = mockId)
  }
}

object DeleteMockRequest {
  implicit val encoder = DeriveJsonEncoder.gen[DeleteMockRequest]
  implicit val decoder = DeriveJsonDecoder.gen[DeleteMockRequest]
}
