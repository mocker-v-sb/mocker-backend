package com.mocker.rest.model

import java.time.Instant

case class Service(
    id: Long,
    name: String,
    path: String,
    url: Option[String],
    description: Option[String],
    createTime: Instant,
    updateTime: Instant,
    expirationTime: Option[Instant]
)
