package com.mocker.rest.model

import com.mocker.rest.api.CommonConverters.convertModelResponse
import com.mocker.rest.request.KVPair
import com.mocker.rest.utils.AvroSchemaUtils
import org.apache.avro.Schema
import org.apache.avro.util.RandomData

case class MockQueryResponse(statusCode: Int, headers: Seq[KVPair], content: String)

object MockQueryResponse {

  def fromMockResponse(mockResponse: MockResponse): MockQueryResponse = {
    MockQueryResponse(
      statusCode = mockResponse.statusCode,
      headers = mockResponse.responseHeaders.toSeq,
      content = mockResponse.response
    )
  }

  def fromModel(model: Model): MockQueryResponse = {
    MockQueryResponse(
      statusCode = 200,
      headers = Seq.empty,
      content = convertModelResponse(model.responseType, model.response)
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
