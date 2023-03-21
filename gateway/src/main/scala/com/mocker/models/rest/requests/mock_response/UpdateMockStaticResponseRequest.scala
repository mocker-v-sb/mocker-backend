package com.mocker.models.rest.requests.mock_response

import com.mocker.models.rest.common.KVPair
import com.mocker.rest.rest_service.UpdateMockStaticResponse.{Request => ProtoUpdateMockStaticResponseRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class UpdateMockStaticResponseRequest(
    servicePath: String = "",
    mockId: Long = 0,
    responseId: Long = 0,
    name: String,
    statusCode: Int,
    requestHeaders: Set[KVPair],
    responseHeaders: Set[KVPair],
    queryParams: Set[KVPair],
    pathParams: Set[KVPair],
    responseContent: String
) {

  def toMessage: ProtoUpdateMockStaticResponseRequest = {
    ProtoUpdateMockStaticResponseRequest(
      servicePath = servicePath,
      mockId = mockId,
      responseId = responseId,
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

object UpdateMockStaticResponseRequest {
  implicit val encoder = DeriveJsonEncoder.gen[UpdateMockStaticResponseRequest]
  implicit val decoder = DeriveJsonDecoder.gen[UpdateMockStaticResponseRequest]
}
