package com.mocker.models.rest.responses

import com.mocker.rest.model.{ModelSnippet => ProtoModelSnippet}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class ModelSnippet(modelId: Long, name: String, description: Option[String])

object ModelSnippet {

  implicit val encoder = DeriveJsonEncoder.gen[ModelSnippet]
  implicit val decoder = DeriveJsonDecoder.gen[ModelSnippet]

  def fromMessage(message: ProtoModelSnippet): ModelSnippet = {
    ModelSnippet(
      modelId = message.modelId,
      name = message.name,
      description = message.description
    )
  }
}
