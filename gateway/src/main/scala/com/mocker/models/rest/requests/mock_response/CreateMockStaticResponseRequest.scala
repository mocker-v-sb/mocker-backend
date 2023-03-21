package com.mocker.models.rest.requests.mock_response

import com.mocker.models.rest.common.KVPair
import com.mocker.rest.rest_service.CreateMockStaticResponse.{Request => ProtoCreateMockStaticResponseRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class CreateMockStaticResponseRequest(
    servicePath: String = "",
    mockId: Long = 0,
    name: String,
    statusCode: Int,
    requestHeaders: Set[KVPair],
    responseHeaders: Set[KVPair],
    queryParams: Set[KVPair],
    pathParams: Set[KVPair],
    responseContent: String
) {

  def toMessage: ProtoCreateMockStaticResponseRequest = {
    ProtoCreateMockStaticResponseRequest(
      servicePath = servicePath,
      mockId = mockId,
      name = name,
      statusCode = statusCode,
      requestHeaders = requestHeaders.map(_.toProto).toSeq,
      responseHeaders = responseHeaders.map(_.toProto).toSeq,
      queryParams = queryParams.map(_.toProto).toSeq,
      pathParams = pathParams.map(_.toProto).toSeq,
      responseContent = responseContent
    )
  }
}

object CreateMockStaticResponseRequest {
  implicit val encoder = DeriveJsonEncoder.gen[CreateMockStaticResponseRequest]
  implicit val decoder = DeriveJsonDecoder.gen[CreateMockStaticResponseRequest]
}
