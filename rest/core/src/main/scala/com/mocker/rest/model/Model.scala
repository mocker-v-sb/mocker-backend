package com.mocker.rest.model

case class Model(
    id: Long = 0,
    serviceId: Long = 0,
    name: String,
    description: Option[String],
    schema: String
)
