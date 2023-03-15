package com.mocker.rest.model

import com.mocker.rest.request.KVPair
import play.api.libs.json.JsValue

case class MockResponse(
    id: Long = 0,
    mockId: Long,
    name: String,
    statusCode: Int,
    requestHeaders: Seq[KVPair],
    responseHeaders: Seq[KVPair],
    pathParams: Seq[KVPair],
    queryParams: Seq[KVPair],
    response: JsValue
)
