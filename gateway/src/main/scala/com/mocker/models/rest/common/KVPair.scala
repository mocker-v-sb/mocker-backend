package com.mocker.models.rest.common

import com.mocker.rest.request.{KVPair => ProtoKVPair}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder}

case class KVPair(name: String, value: String) {

  def toProto: ProtoKVPair = {
    ProtoKVPair(name = name, value = value)
  }
}

// todo: ???
case class Helper(name: String, value: String)

object Helper {
  implicit val decoder: JsonDecoder[Helper] = DeriveJsonDecoder.gen[Helper]
}

object KVPair {
  implicit val encoder = DeriveJsonEncoder.gen[KVPair]
  implicit val decoder = JsonDecoder[Helper].map(h => KVPair(h.name, h.value))

  def fromProto(protoKVPair: ProtoKVPair): KVPair = {
    KVPair(protoKVPair.name, protoKVPair.value)
  }
}
