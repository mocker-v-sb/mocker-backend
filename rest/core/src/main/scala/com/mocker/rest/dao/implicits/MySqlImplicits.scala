package com.mocker.rest.dao.implicits

import com.mocker.rest.request.{KVPair, Method}
import play.api.libs.json.{JsValue, Json, OFormat}
import slick.jdbc.H2Profile.{BaseColumnType, MappedColumnType}
import slick.jdbc.MySQLProfile.api._

object MySqlImplicits {

  implicit val ExtendedReasonFormat: OFormat[KVPair] = Json.format[KVPair]

  implicit val stringSequenceCT: BaseColumnType[Seq[String]] =
    MappedColumnType.base[Seq[String], String](
      seq => Json.toJson(seq).toString,
      string => Json.parse(string).as[Seq[String]]
    )

  implicit val headersCT: BaseColumnType[Seq[KVPair]] =
    MappedColumnType.base[Seq[KVPair], String](
      headers => Json.toJson(headers).toString,
      string => Json.parse(string).as[Seq[KVPair]]
    )

  implicit val methodCT: BaseColumnType[Method] =
    MappedColumnType.base[Method, Int](
      method => method.value,
      code => Method.fromValue(code)
    )

  implicit val jsValueCT: BaseColumnType[JsValue] =
    MappedColumnType.base[JsValue, String](
      jsValue => jsValue.toString,
      string => Json.parse(string)
    )

}
