package com.mocker.models.rest.responses.mock

import com.mocker.rest.rest_service.GetAllServiceMocks.{Response => ProtoGetAllServiceMocksResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetAllServiceMocksResponse(mocks: Seq[MockSnippet])

object GetAllServiceMocksResponse {

  implicit val encoder = DeriveJsonEncoder.gen[GetAllServiceMocksResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetAllServiceMocksResponse]

  def fromMessage(message: ProtoGetAllServiceMocksResponse): GetAllServiceMocksResponse = {
    GetAllServiceMocksResponse(
      message.mocks.map(MockSnippet.fromMessage)
    )
  }
}
