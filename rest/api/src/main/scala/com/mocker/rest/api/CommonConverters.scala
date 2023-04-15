package com.mocker.rest.api

import com.mocker.rest.model.ResponseTypeNamespace.ResponseType
import com.mocker.rest.utils.AvroSchemaUtils

object CommonConverters {

  def convertModelResponse(responseType: ResponseType, response: String): String = {
    responseType match {
      case ResponseType.PLAINTEXT | ResponseType.JSON | ResponseType.XML => response
      case ResponseType.JSON_TEMPLATE                                    => AvroSchemaUtils.generateSample(response)
    }
  }

  def convertModelSchema(responseType: ResponseType, response: String): String = {
    responseType match {
      case ResponseType.PLAINTEXT | ResponseType.JSON | ResponseType.XML => response
      case ResponseType.JSON_TEMPLATE                                    => AvroSchemaUtils.fromSample(response)
    }
  }

}
