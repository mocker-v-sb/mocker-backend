package com.mocker.rest.model

import com.mocker.rest.request.{Header, Method}

import java.sql.Timestamp

case class Mock(
    id: Long = 0,
    serviceId: Long = 0,
    name: String,
    description: Option[String],
    path: String,
    method: Method,
    requestModelId: Option[Long],
    responseModelId: Option[Long],
    requestHeaders: Seq[Header],
    responseHeaders: Seq[Header],
    queryParams: Seq[String],
    pathParams: Seq[String],
    creationTime: Timestamp
)
