package com.mocker.models.rest.requests.mock_response

import com.mocker.rest.rest_service.DeleteAllMockStaticResponses.{Request => ProtoDeleteAllMockStaticResponsesRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class DeleteAllMockStaticResponsesRequest(servicePath: String, mockId: Long) {

  def toMessage: ProtoDeleteAllMockStaticResponsesRequest = {
    ProtoDeleteAllMockStaticResponsesRequest(servicePath = servicePath, mockId = mockId)
  }
}

object DeleteAllMockStaticResponsesRequest {
  implicit val encoder = DeriveJsonEncoder.gen[DeleteAllMockStaticResponsesRequest]
  implicit val decoder = DeriveJsonDecoder.gen[DeleteAllMockStaticResponsesRequest]
}
