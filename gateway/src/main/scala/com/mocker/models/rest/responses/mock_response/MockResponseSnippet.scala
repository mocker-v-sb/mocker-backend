package com.mocker.models.rest.responses.mock_response

import com.mocker.models.rest.common.Method
import com.mocker.rest.mock_response.{MockResponseSnippet => ProtoMockResponseSnippet}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class MockResponseSnippet(responseId: Long, name: String, statusCode: Int, fullPath: String)

object MockResponseSnippet {

  implicit val encoder = DeriveJsonEncoder.gen[MockResponseSnippet]
  implicit val decoder = DeriveJsonDecoder.gen[MockResponseSnippet]

  def fromMessage(message: ProtoMockResponseSnippet): MockResponseSnippet = {
    MockResponseSnippet(
      responseId = message.responseId,
      name = message.name,
      statusCode = message.statusCode,
      fullPath = message.fullPath
    )
  }
}
