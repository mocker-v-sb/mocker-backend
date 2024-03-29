package com.mocker.models.rest.requests.service

import com.google.protobuf.timestamp.{Timestamp => GoogleTimestamp}
import com.google.protobuf.util.Timestamps
import com.mocker.rest.rest_service.UpdateService.{Request => ProtoUpdateServiceRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class UpdateServiceRequest(
    servicePath: String = "",
    name: String,
    path: String,
    url: Option[String],
    description: Option[String],
    expirationTime: Option[Long],
    isProxyEnabled: Boolean,
    isHistoryEnabled: Boolean
) {

  def toMessage: ProtoUpdateServiceRequest = {
    ProtoUpdateServiceRequest(
      servicePath = servicePath,
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

object UpdateServiceRequest {
  implicit val encoder = DeriveJsonEncoder.gen[UpdateServiceRequest]
  implicit val decoder = DeriveJsonDecoder.gen[UpdateServiceRequest]
}
