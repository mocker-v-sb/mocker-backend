package com.mocker.rest.api

import com.mocker.rest.model.{Mock, MockPatch, MockResponse, Model, Service}
import com.mocker.rest.rest_service._

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

  def convertUpdateServiceRequest(request: UpdateService.Request): Service = {
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
      schema = request.schema
    )
  }

  def convertUpdateModelRequest(request: UpdateModel.Request): Model = {
    Model(
      name = request.name,
      description = request.description,
      schema = request.schema
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
      pathParams = request.pathParams
    )
  }

  def convertUpdateMockRequest(request: UpdateMock.Request): MockPatch = {
    MockPatch(
      name = request.name,
      description = request.description,
      method = request.method,
      requestModelId = request.requestModelId,
      responseModelId = request.responseModelId
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
      response = request.responseContent
    )
  }

  def convertUpdateMockResponseRequest(request: UpdateMockStaticResponse.Request): MockResponse = {
    MockResponse(
      mockId = request.mockId,
      name = request.name,
      statusCode = request.statusCode,
      requestHeaders = request.requestHeaders,
      responseHeaders = request.responseHeaders,
      queryParams = request.queryParams,
      pathParams = request.pathParams,
      response = request.responseContent
    )
  }

}
