package com.mocker.models.rest.requests.model

import com.mocker.rest.rest_service.CreateModel.{Request => ProtoCreateModelRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class CreateModelRequest(servicePath: String = "", name: String, description: Option[String], sample: String) {

  def toMessage: ProtoCreateModelRequest = {
    ProtoCreateModelRequest(servicePath = servicePath, name = name, description = description, sample = sample)
  }
}

object CreateModelRequest {
  implicit val encoder = DeriveJsonEncoder.gen[CreateModelRequest]
  implicit val decoder = DeriveJsonDecoder.gen[CreateModelRequest]
}
