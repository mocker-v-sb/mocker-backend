package com.mocker.models.rest.responses.service

import com.mocker.rest.rest_service.GetService.{Response => ProtoGetServiceResponse}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetServiceResponse(
    name: String,
    path: String,
    description: Option[String],
    url: Option[String],
    creationTime: Option[Long],
    updateTime: Option[Long],
    expirationTime: Option[Long]
)

object GetServiceResponse {
  implicit val encoder = DeriveJsonEncoder.gen[GetServiceResponse]
  implicit val decoder = DeriveJsonDecoder.gen[GetServiceResponse]

  def fromMessage(message: ProtoGetServiceResponse): GetServiceResponse = {
    GetServiceResponse(
      name = message.name,
      path = message.path,
      description = message.description,
      url = message.url,
      creationTime = message.creationTime.map(_.asJavaInstant.toEpochMilli),
      updateTime = message.updateTime.map(_.asJavaInstant.toEpochMilli),
      expirationTime = message.expirationTime.map(_.asJavaInstant.toEpochMilli)
    )
  }
}
