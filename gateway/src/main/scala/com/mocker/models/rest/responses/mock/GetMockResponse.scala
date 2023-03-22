package com.mocker.models.rest.responses.mock

import com.mocker.models.rest.common.Method
import com.mocker.rest.rest_service.GetMock.{Response => ProtoGetMockResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetMockResponse(
    mockId: Long,
    name: String,
    description: Option[String],
    path: String,
    method: Method,
    requestModelId: Option[Long],
    responseModelId: Option[Long],
    requestHeaders: Set[String],
    responseHeaders: Set[String],
    queryParams: Set[String],
    pathParams: Set[String]
)

object GetMockResponse {
  implicit val encoder = DeriveJsonEncoder.gen[GetMockResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetMockResponse]

  def fromMessage(message: ProtoGetMockResponse): GetMockResponse = {
    GetMockResponse(
      mockId = message.id,
      name = message.name,
      description = message.description,
      path = message.path,
      method = Method.forName(message.method.name),
      requestModelId = message.requestModelId,
      responseModelId = message.responseModelId,
      requestHeaders = message.requestHeaders.toSet,
      responseHeaders = message.responseHeaders.toSet,
      queryParams = message.queryParams.toSet,
      pathParams = message.pathParams.toSet
    )
  }
}
