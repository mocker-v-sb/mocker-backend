package com.mocker.models.rest.requests.mock_response

import com.mocker.rest.rest_service.GetAllMockStaticResponses.{Request => ProtoGetAllMockStaticResponsesRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetAllMockStaticResponsesRequest(servicePath: String, mockId: Long) {

  def toMessage: ProtoGetAllMockStaticResponsesRequest = {
    ProtoGetAllMockStaticResponsesRequest(servicePath = servicePath, mockId = mockId)
  }
}

object GetAllMockStaticResponsesRequest {
  implicit val encoder = DeriveJsonEncoder.gen[GetAllMockStaticResponsesRequest]
  implicit val decoder = DeriveJsonDecoder.gen[GetAllMockStaticResponsesRequest]
}
