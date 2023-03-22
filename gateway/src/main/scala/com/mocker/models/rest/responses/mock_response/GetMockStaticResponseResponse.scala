package com.mocker.models.rest.responses.mock_response

import com.mocker.models.rest.common.KVPair
import com.mocker.rest.rest_service.GetMockStaticResponse.{Response => ProtoGetMockStaticResponseResponse}
import zio.json.DeriveJsonEncoder

case class GetMockStaticResponseResponse(
    responseId: Long,
    name: String,
    statusCode: Int,
    requestHeaders: Set[KVPair],
    responseHeaders: Set[KVPair],
    queryParams: Set[KVPair],
    pathParams: Set[KVPair],
    responseContent: String
)

object GetMockStaticResponseResponse {
  implicit val encoder = DeriveJsonEncoder.gen[GetMockStaticResponseResponse]

  def fromMessage(message: ProtoGetMockStaticResponseResponse): GetMockStaticResponseResponse = {
    GetMockStaticResponseResponse(
      responseId = message.id,
      name = message.name,
      statusCode = message.statusCode,
      requestHeaders = message.requestHeaders.map(v => KVPair.fromProto(v)).toSet,
      responseHeaders = message.responseHeaders.map(v => KVPair.fromProto(v)).toSet,
      queryParams = message.queryParams.map(v => KVPair.fromProto(v)).toSet,
      pathParams = message.pathParams.map(v => KVPair.fromProto(v)).toSet,
      responseContent = message.responseContent
    )
  }
}
