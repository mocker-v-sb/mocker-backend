package com.mocker.models.rest.requests.mock_response

import com.mocker.rest.rest_service.DeleteMockStaticResponse.{Request => ProtoDeleteMockStaticResponseRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class DeleteMockStaticResponseRequest(servicePath: String, mockId: Long, responseId: Long) {

  def toMessage: ProtoDeleteMockStaticResponseRequest = {
    ProtoDeleteMockStaticResponseRequest(servicePath = servicePath, mockId = mockId, responseId = responseId)
  }
}

object DeleteMockStaticResponseRequest {
  implicit val encoder = DeriveJsonEncoder.gen[DeleteMockStaticResponseRequest]
  implicit val decoder = DeriveJsonDecoder.gen[DeleteMockStaticResponseRequest]
}
