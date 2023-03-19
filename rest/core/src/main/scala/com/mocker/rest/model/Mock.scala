package com.mocker.rest.model

import com.mocker.rest.request.Method

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
    requestHeaders: Seq[String],
    responseHeaders: Seq[String],
    queryParams: Seq[String],
    pathParams: Seq[String]
)

case class MockPatch(
    name: String,
    description: Option[String],
    method: Method,
    requestModelId: Option[Long],
    responseModelId: Option[Long]
)
