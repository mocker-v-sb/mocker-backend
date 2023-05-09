package com.mocker.rest.model

import com.mocker.rest.request.KVPair

case class MockResponse(
    id: Long = 0,
    mockId: Long,
    name: String,
    statusCode: Int,
    requestHeaders: Set[KVPair],
    responseHeaders: Set[KVPair],
    pathParams: Set[KVPair],
    queryParams: Set[KVPair],
    response: String
)
