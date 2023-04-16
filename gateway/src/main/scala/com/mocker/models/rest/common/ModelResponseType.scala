package com.mocker.models.rest.common

import com.mocker.rest.model.ResponseTypeNamespace.{ResponseType => ProtoResponseType}
import zio.json.{JsonDecoder, JsonEncoder}

sealed trait ModelResponseType {

  val proto: ProtoResponseType

  def name: String = proto.name
}

object ModelResponseType {

  def default: ModelResponseType = PLAINTEXT

  def all: Seq[ModelResponseType] = Seq(PLAINTEXT, XML, JSON, JSON_TEMPLATE)

  implicit val encoder: JsonEncoder[ModelResponseType] = JsonEncoder[String].contramap(_.name)
  implicit val decoder: JsonDecoder[ModelResponseType] =
    JsonDecoder[String].map(s => ModelResponseType.forName(s.toUpperCase))

  def forName(name: String): ModelResponseType = {
    all
      .find(_.name == name)
      .getOrElse(throw new IllegalArgumentException(s"Not found model's response type for name $name"))
  }
}

case object PLAINTEXT extends ModelResponseType {
  override val proto: ProtoResponseType = ProtoResponseType.PLAINTEXT
}

case object XML extends ModelResponseType {
  override val proto: ProtoResponseType = ProtoResponseType.XML
}

case object JSON extends ModelResponseType {
  override val proto: ProtoResponseType = ProtoResponseType.JSON
}

case object JSON_TEMPLATE extends ModelResponseType {
  override val proto: ProtoResponseType = ProtoResponseType.JSON_TEMPLATE
}
