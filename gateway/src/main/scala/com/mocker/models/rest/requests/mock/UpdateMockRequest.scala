package com.mocker.models.rest.requests.mock

import com.mocker.models.rest.common.Method
import com.mocker.rest.rest_service.UpdateMock.{Request => ProtoUpdateMockRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class UpdateMockRequest(
    servicePath: String = "",
    mockId: Long = 0,
    name: String,
    description: Option[String],
    method: Method,
    requestModelId: Option[Long],
    responseModelId: Option[Long]
) {

  def toMessage: ProtoUpdateMockRequest = {
    ProtoUpdateMockRequest(
      servicePath = servicePath,
      mockId = mockId,
      name = name,
      description = description,
      method = method.proto,
      requestModelId = requestModelId,
      responseModelId = responseModelId
    )
  }
}

object UpdateMockRequest {
  implicit val encoder = DeriveJsonEncoder.gen[UpdateMockRequest]
  implicit val decoder = DeriveJsonDecoder.gen[UpdateMockRequest]
}
