package com.mocker.models.rest.responses.mock

import com.mocker.models.rest.common.Method
import com.mocker.rest.mock.{MockSnippet => ProtoMockSnippet}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class MockSnippet(mockId: Long, name: String, description: Option[String], path: String, method: Method)

object MockSnippet {

  implicit val encoder = DeriveJsonEncoder.gen[MockSnippet]
  implicit val decoder = DeriveJsonDecoder.gen[MockSnippet]

  def fromMessage(message: ProtoMockSnippet): MockSnippet = {
    MockSnippet(
      mockId = message.mockId,
      name = message.name,
      description = message.description,
      path = message.path,
      method = Method.forName(message.method.name)
    )
  }
}
