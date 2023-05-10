package com.mocker.rest.model

import java.time.Instant

case class Service(
    id: Long = 0,
    name: String,
    path: String,
    url: Option[String],
    description: Option[String],
    creationTime: Instant,
    updateTime: Instant,
    expirationTime: Option[Instant],
    isProxyEnabled: Boolean,
    isHistoryEnabled: Boolean,
    owner: String = ""
)
