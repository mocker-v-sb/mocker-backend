package com.mocker.models.rest.common

import com.mocker.rest.request.{KVPair => ProtoKVPair}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class KVPair(name: String, value: String) {

  def toProto: ProtoKVPair = {
    ProtoKVPair(name = name, value = value)
  }
}

object KVPair {
  implicit val encoder = DeriveJsonEncoder.gen[KVPair]
  implicit val decoder = DeriveJsonDecoder.gen[KVPair]

  def fromProto(protoKVPair: ProtoKVPair): KVPair = {
    KVPair(protoKVPair.name, protoKVPair.value)
  }
}
