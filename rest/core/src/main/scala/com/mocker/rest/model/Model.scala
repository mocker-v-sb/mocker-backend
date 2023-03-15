package com.mocker.rest.model

import play.api.libs.json.JsValue

import java.sql.Timestamp

case class Model(
    id: Long = 0,
    serviceId: Long = 0,
    name: String,
    description: Option[String],
    schema: JsValue,
    creationTime: Timestamp
)
