package com.mocker.rest.model

import com.mocker.rest.request.{KVPair, Method}

case class MockQuery(
    rawUrl: String,
    servicePath: String,
    requestPath: String,
    method: Method,
    body: Option[String],
    headers: Seq[KVPair],
    queryParams: Seq[KVPair]
)
