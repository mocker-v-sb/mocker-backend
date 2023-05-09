package com.mocker.rest.model

import com.mocker.rest.mock_history.ResponseSourceNamespace.ResponseSource
import com.mocker.rest.request.{KVPair, Method}

import java.time.Instant

case class MockHistoryItem(
    id: Long = 0,
    serviceId: Long,
    method: Method,
    queryUrl: String,
    responseUrl: String,
    responseSource: ResponseSource,
    statusCode: Int,
    requestHeaders: Seq[KVPair],
    responseHeaders: Seq[KVPair],
    responseTime: Instant,
    response: String
)
