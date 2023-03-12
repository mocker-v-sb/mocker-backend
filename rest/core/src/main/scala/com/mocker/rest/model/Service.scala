package com.mocker.rest.model

import java.sql.Timestamp

case class Service(
    id: Long = 0,
    name: String,
    path: String,
    url: Option[String],
    description: Option[String],
    createTime: Timestamp,
    updateTime: Timestamp,
    expirationTime: Option[Timestamp]
)
