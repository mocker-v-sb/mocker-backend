package com.mocker.models.rest.requests.mock_response

import com.mocker.rest.rest_service.GetMockStaticResponse.{Request => ProtoGetMockStaticResponseRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetMockStaticResponseRequest(servicePath: String, mockId: Long, responseId: Long) {

  def toMessage: ProtoGetMockStaticResponseRequest = {
    ProtoGetMockStaticResponseRequest(servicePath = servicePath, mockId = mockId, responseId = responseId)
  }
}

object GetMockStaticResponseRequest {
  implicit val encoder = DeriveJsonEncoder.gen[GetMockStaticResponseRequest]
  implicit val decoder = DeriveJsonDecoder.gen[GetMockStaticResponseRequest]
}
