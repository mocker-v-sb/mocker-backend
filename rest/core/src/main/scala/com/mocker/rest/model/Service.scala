package com.mocker.rest.model

import java.time.Instant

case class Service(
    id: Long,
    name: String,
    url: Option[String],
    createTime: Instant,
    updateTime: Instant,
    expirationTime: Option[Instant]
)
