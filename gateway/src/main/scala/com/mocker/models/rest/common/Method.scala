package com.mocker.models.rest.common

import com.mocker.rest.request.{Method => ProtoMethod}
import zio.json.{JsonDecoder, JsonEncoder}

sealed trait Method {

  def name: String

  def toProto: ProtoMethod
}

object Method {

  def default: Method = GET

  def all: Seq[Method] = Seq(GET, POST, PUT, DELETE, PATCH)

  implicit val encoder: JsonEncoder[Method] = JsonEncoder[String].contramap(_.name)
  implicit val decoder: JsonDecoder[Method] = JsonDecoder[String].map(s => Method.forName(s.toUpperCase))

  def forName(name: String): Method = {
    all.find(_.name == name).getOrElse(throw new IllegalArgumentException(s"Not found method for name $name"))
  }
}

case object GET extends Method {
  override def name: String = "GET"

  override def toProto: ProtoMethod = ProtoMethod.GET
}

case object POST extends Method {
  override def name: String = "POST"

  override def toProto: ProtoMethod = ProtoMethod.POST
}

case object PUT extends Method {
  override def name: String = "PUT"

  override def toProto: ProtoMethod = ProtoMethod.PUT
}

case object DELETE extends Method {
  override def name: String = "DELETE"

  override def toProto: ProtoMethod = ProtoMethod.DELETE
}

case object PATCH extends Method {
  override def name: String = "PATCH"

  override def toProto: ProtoMethod = ProtoMethod.PATCH
}
