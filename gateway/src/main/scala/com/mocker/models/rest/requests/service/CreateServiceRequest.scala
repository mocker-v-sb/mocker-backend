package com.mocker.models.rest.requests.service

import com.google.protobuf.timestamp.{Timestamp => GoogleTimestamp}
import com.google.protobuf.util.Timestamps
import com.mocker.rest.rest_service.CreateService.{Request => ProtoCreateServiceRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class CreateServiceRequest(
    name: String,
    path: String,
    url: Option[String],
    description: Option[String],
    expirationTime: Option[Long],
    isProxyEnabled: Boolean,
    isHistoryEnabled: Boolean
) {

  def toMessage: ProtoCreateServiceRequest = {
    ProtoCreateServiceRequest(
      name = name,
      path = path,
      url = url,
      description = description,
      expirationTime = expirationTime.map(Timestamps.fromMillis).map(GoogleTimestamp.fromJavaProto),
      isProxyEnabled = isProxyEnabled,
      isHistoryEnabled = isHistoryEnabled
    )
  }
}

object CreateServiceRequest {
  implicit val encoder = DeriveJsonEncoder.gen[CreateServiceRequest]
  implicit val decoder = DeriveJsonDecoder.gen[CreateServiceRequest]
}
