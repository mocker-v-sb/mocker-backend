package com.mocker.models.rest.requests.mock

import com.mocker.models.rest.common.Method
import com.mocker.rest.rest_service.CreateMock.{Request => ProtoCreateMockRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class CreateMockRequest(
    servicePath: String = "",
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
) {

  def toMessage: ProtoCreateMockRequest = {
    ProtoCreateMockRequest(
      servicePath = servicePath,
      name = name,
      description = description,
      path = path,
      method = method.proto,
      requestModelId = requestModelId,
      responseModelId = responseModelId,
      requestHeaders = requestHeaders.toSeq,
      responseHeaders = responseHeaders.toSeq,
      queryParams = queryParams.toSeq,
      pathParams = pathParams.toSeq
    )
  }
}

object CreateMockRequest {
  implicit val encoder = DeriveJsonEncoder.gen[CreateMockRequest]
  implicit val decoder = DeriveJsonDecoder.gen[CreateMockRequest]
}
