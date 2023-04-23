package com.mocker.rest.dao.implicits

import com.mocker.rest.mock_history.ResponseSourceNamespace.ResponseSource
import com.mocker.rest.model.ResponseTypeNamespace.ResponseType
import com.mocker.rest.request.{KVPair, Method}
import slick.jdbc.H2Profile.{BaseColumnType, MappedColumnType}
import slick.jdbc.MySQLProfile.api._
import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}

object MySqlImplicits {

  implicit val kvPairDecoder: JsonDecoder[KVPair] = DeriveJsonDecoder.gen[KVPair]

  implicit val kvPairEncoder: JsonEncoder[KVPair] = DeriveJsonEncoder.gen[KVPair]

  implicit val stringSequenceCT: BaseColumnType[Seq[String]] =
    MappedColumnType.base[Seq[String], String](
      seq => seq.toJson,
      string => string.fromJson[Seq[String]].getOrElse(Seq.empty)
    )

  implicit val headersCT: BaseColumnType[Seq[KVPair]] =
    MappedColumnType.base[Seq[KVPair], String](
      headers => headers.toJson,
      string => string.fromJson[Seq[KVPair]].getOrElse(Seq.empty)
    )

  implicit val methodCT: BaseColumnType[Method] =
    MappedColumnType.base[Method, Int](
      method => method.value,
      code => Method.fromValue(code)
    )

  implicit val modelResponseTypeCT: BaseColumnType[ResponseType] =
    MappedColumnType.base[ResponseType, Int](
      responseType => responseType.value,
      code => ResponseType.fromValue(code)
    )

  implicit val responseSourceCT: BaseColumnType[ResponseSource] =
    MappedColumnType.base[ResponseSource, Int](
      responseSource => responseSource.value,
      code => ResponseSource.fromValue(code)
    )
}
