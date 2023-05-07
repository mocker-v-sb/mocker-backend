package com.mocker.models.rest.common

import com.mocker.rest.mock_history.{ResponseTimeSort => ProtoResponseTimeSort}
import zio.json.{JsonDecoder, JsonEncoder}

sealed trait ResponseTimeSort {

  val proto: ProtoResponseTimeSort

  def name: String = proto.name
}

object ResponseTimeSort {

  def default: ResponseTimeSort = DESC

  def all: Seq[ResponseTimeSort] = Seq(DESC, ASC)

  implicit val encoder: JsonEncoder[ResponseTimeSort] = JsonEncoder[String].contramap(_.name)
  implicit val decoder: JsonDecoder[ResponseTimeSort] =
    JsonDecoder[String].map(s => ResponseTimeSort.forName(s.toUpperCase))

  def forName(name: String): ResponseTimeSort = {
    all.find(_.name == name).getOrElse(throw new IllegalArgumentException(s"Not found response source for name $name"))
  }

  def forNameOpt(name: String): Option[ResponseTimeSort] = {
    all.find(_.name == name)
  }
}

case object DESC extends ResponseTimeSort {
  override val proto: ProtoResponseTimeSort = ProtoResponseTimeSort.DESC
}

case object ASC extends ResponseTimeSort {
  override val proto: ProtoResponseTimeSort = ProtoResponseTimeSort.ASC
}
