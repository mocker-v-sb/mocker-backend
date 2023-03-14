package com.mocker.rest.dao.implicits

import com.mocker.rest.request.{Header, Method}
import play.api.libs.json.{Json, OFormat}
import slick.jdbc.H2Profile.{BaseColumnType, MappedColumnType}
import slick.jdbc.MySQLProfile.api._

object MySqlImplicits {

  implicit val ExtendedReasonFormat: OFormat[Header] = Json.format[Header]

  implicit val stringSequenceCT: BaseColumnType[Seq[String]] =
    MappedColumnType.base[Seq[String], String](
      seq => Json.toJson(seq).toString(),
      string => Json.parse(string).as[Seq[String]]
    )

  implicit val headersCT: BaseColumnType[Seq[Header]] =
    MappedColumnType.base[Seq[Header], String](
      headers => Json.toJson(headers).toString(),
      string => Json.parse(string).as[Seq[Header]]
    )

  implicit val methodCT: BaseColumnType[Method] =
    MappedColumnType.base[Method, Int](
      method => method.value,
      code => Method.fromValue(code)
    )

}
