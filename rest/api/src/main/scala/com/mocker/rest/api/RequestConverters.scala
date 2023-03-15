package com.mocker.rest.api

import com.mocker.rest.model.{Mock, MockResponse, Model, Service}
import com.mocker.rest.rest_service._
import play.api.libs.json.Json

import java.sql.Timestamp
import java.time.Instant

object RequestConverters {

  def convertCreateServiceRequest(request: CreateService.Request): Service = {
    Service(
      name = request.name,
      path = request.path,
      url = request.url,
      description = request.description,
      creationTime = Timestamp.from(Instant.now()),
      updateTime = Timestamp.from(Instant.now()),
      expirationTime = request.expirationTime.map(t => Timestamp.from(t.asJavaInstant))
    )
  }

  def convertCreateModelRequest(request: CreateModel.Request): Model = {
    Model(
      name = request.name,
      description = request.description,
      schema = Json.parse(request.schema),
      creationTime = Timestamp.from(Instant.now())
    )
  }

  def convertCreateMockRequest(request: CreateMock.Request): Mock = {
    Mock(
      name = request.name,
      description = request.description,
      path = request.path,
      method = request.method,
      requestModelId = request.requestModelId,
      responseModelId = request.responseModelId,
      requestHeaders = request.requestHeaders,
      responseHeaders = request.responseHeaders,
      queryParams = request.queryParams,
      pathParams = request.pathParams,
      creationTime = Timestamp.from(Instant.now())
    )
  }

  def convertCreateMockResponseRequest(request: CreateMockStaticResponse.Request): MockResponse = {
    MockResponse(
      mockId = request.mockId,
      name = request.name,
      statusCode = request.statusCode,
      requestHeaders = request.requestHeaders,
      responseHeaders = request.responseHeaders,
      queryParams = request.queryParams,
      pathParams = request.pathParams,
      response = Json.parse(request.responseContent)
    )
  }

}
