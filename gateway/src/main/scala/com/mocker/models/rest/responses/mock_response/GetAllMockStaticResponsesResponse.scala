package com.mocker.models.rest.responses.mock_response

import com.mocker.rest.rest_service.GetAllMockStaticResponses.{Response => ProtoGetAllMockStaticResponsesResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetAllMockStaticResponsesResponse(responses: Seq[MockResponseSnippet])

object GetAllMockStaticResponsesResponse {

  implicit val encoder = DeriveJsonEncoder.gen[GetAllMockStaticResponsesResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetAllMockStaticResponsesResponse]

  def fromMessage(message: ProtoGetAllMockStaticResponsesResponse): GetAllMockStaticResponsesResponse = {
    GetAllMockStaticResponsesResponse(
      message.mockResponses.map(MockResponseSnippet.fromMessage)
    )
  }
}
