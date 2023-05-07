package com.mocker.models.rest.common

import com.mocker.rest.request.{Method => ProtoMethod}
import zio.json.{JsonDecoder, JsonEncoder}

sealed trait Method {

  val proto: ProtoMethod

  def name: String = proto.name
}

object Method {

  def default: Method = GET

  def all: Seq[Method] = Seq(GET, POST, PUT, DELETE, PATCH)

  implicit val encoder: JsonEncoder[Method] = JsonEncoder[String].contramap(_.name)
  implicit val decoder: JsonDecoder[Method] = JsonDecoder[String].map(s => Method.forName(s.toUpperCase))

  def forName(name: String): Method = {
    all.find(_.name == name).getOrElse(throw new IllegalArgumentException(s"Not found method for name $name"))
  }

  def forNameOpt(name: String): Option[Method] = {
    all.find(_.name == name)
  }
}

case object GET extends Method {
  override val proto: ProtoMethod = ProtoMethod.GET
}

case object POST extends Method {
  override val proto: ProtoMethod = ProtoMethod.POST
}

case object PUT extends Method {
  override val proto: ProtoMethod = ProtoMethod.PUT
}

case object DELETE extends Method {
  override val proto: ProtoMethod = ProtoMethod.DELETE
}

case object PATCH extends Method {
  override val proto: ProtoMethod = ProtoMethod.PATCH
}
