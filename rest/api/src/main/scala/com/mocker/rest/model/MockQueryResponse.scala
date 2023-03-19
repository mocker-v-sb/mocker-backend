package com.mocker.rest.model

import com.mocker.rest.request.KVPair
import org.apache.avro.Schema
import org.apache.avro.util.RandomData

case class MockQueryResponse(statusCode: Int, headers: Seq[KVPair], content: String)

object MockQueryResponse {

  def fromMockResponse(mockResponse: MockResponse): MockQueryResponse = {
    MockQueryResponse(
      statusCode = mockResponse.statusCode,
      headers = mockResponse.responseHeaders,
      content = mockResponse.response
    )
  }

  def fromModel(model: Model): MockQueryResponse = {
    val schema = new Schema.Parser().parse(model.schema)
    val data = new RandomData(schema, 1).iterator.next()
    MockQueryResponse(
      statusCode = 200,
      headers = Seq.empty,
      content = String.valueOf(data)
    )
  }

  def default: MockQueryResponse = {
    MockQueryResponse(
      statusCode = 200,
      headers = Seq.empty,
      content = "{}"
    )
  }
}
