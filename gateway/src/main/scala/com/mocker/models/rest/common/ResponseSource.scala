package com.mocker.models.rest.common

import com.mocker.rest.mock_history.ResponseSourceNamespace.{ResponseSource => ProtoResponseSource}
import zio.json.{JsonDecoder, JsonEncoder}

sealed trait ResponseSource {

  val proto: ProtoResponseSource

  def name: String = proto.name
}

object ResponseSource {

  def default: ResponseSource = EMPTY

  def all: Seq[ResponseSource] = Seq(EMPTY, STATIC_RESPONSE, PROXIED_RESPONSE, MOCK_TEMPLATE)

  implicit val encoder: JsonEncoder[ResponseSource] = JsonEncoder[String].contramap(_.name)
  implicit val decoder: JsonDecoder[ResponseSource] =
    JsonDecoder[String].map(s => ResponseSource.forName(s.toUpperCase))

  def forName(name: String): ResponseSource = {
    all.find(_.name == name).getOrElse(throw new IllegalArgumentException(s"Not found response source for name $name"))
  }

  def forNameOpt(name: String): Option[ResponseSource] = {
    all.find(_.name == name)
  }
}

case object EMPTY extends ResponseSource {
  override val proto: ProtoResponseSource = ProtoResponseSource.EMPTY
}

case object STATIC_RESPONSE extends ResponseSource {
  override val proto: ProtoResponseSource = ProtoResponseSource.STATIC_RESPONSE
}

case object PROXIED_RESPONSE extends ResponseSource {
  override val proto: ProtoResponseSource = ProtoResponseSource.PROXIED_RESPONSE
}

case object MOCK_TEMPLATE extends ResponseSource {
  override val proto: ProtoResponseSource = ProtoResponseSource.MOCK_TEMPLATE
}
