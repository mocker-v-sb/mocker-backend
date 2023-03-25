package com.mocker.models.rest.requests

import com.mocker.models.rest.common.{KVPair, Method}
import com.mocker.rest.rest_service.GetResponse.{Request => ProtoGetResponseRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class GetResponseRequest(
    servicePath: String,
    requestPath: String,
    method: Method,
    body: Option[String],
    headers: Set[KVPair],
    queryParams: Set[KVPair]
) {

  def toMessage: ProtoGetResponseRequest = {
    ProtoGetResponseRequest(
      servicePath = servicePath,
      requestPath = requestPath,
      method = method.toProto,
      body = body,
      headers = headers.map(_.toProto).toSeq,
      queryParams = queryParams.map(_.toProto).toSeq
    )
  }
}

object GetResponseRequest {
  implicit val encoder = DeriveJsonEncoder.gen[GetResponseRequest]
  implicit val decoder = DeriveJsonDecoder.gen[GetResponseRequest]
}
