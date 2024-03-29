package com.mocker.rest.api

import com.mocker.rest.api.CommonConverters.convertModelSchema
import com.mocker.rest.model._
import com.mocker.rest.rest_service._
import com.mocker.rest.utils.Orderings._

import java.time.Instant

object RequestConverters {

  def convertCreateServiceRequest(request: CreateService.Request): Service = {
    Service(
      name = request.name,
      path = request.path,
      url = request.url,
      description = request.description,
      creationTime = Instant.now(),
      updateTime = Instant.now(),
      expirationTime = request.expirationTime.map(_.asJavaInstant),
      isProxyEnabled = request.isProxyEnabled,
      isHistoryEnabled = request.isHistoryEnabled
    )
  }

  def convertUpdateServiceRequest(request: UpdateService.Request): Service = {
    Service(
      name = request.name,
      path = request.path,
      url = request.url,
      description = request.description,
      creationTime = Instant.now(),
      updateTime = Instant.now(),
      expirationTime = request.expirationTime.map(t => t.asJavaInstant),
      isProxyEnabled = request.isProxyEnabled,
      isHistoryEnabled = request.isHistoryEnabled
    )
  }

  def convertCreateModelRequest(request: CreateModel.Request): Model = {
    Model(
      name = request.name,
      description = request.description,
      responseType = request.responseType,
      response = convertModelSchema(request.responseType, request.response)
    )
  }

  def convertUpdateModelRequest(request: UpdateModel.Request): Model = {
    Model(
      id = request.modelId,
      name = request.name,
      description = request.description,
      responseType = request.responseType,
      response = convertModelSchema(request.responseType, request.response)
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
      requestHeaders = request.requestHeaders.toSet,
      responseHeaders = request.responseHeaders.toSet,
      queryParams = request.queryParams.toSet,
      pathParams = request.pathParams.toSet,
      response = request.responseContent
    )
  }

  def convertUpdateMockResponseRequest(request: UpdateMockStaticResponse.Request): MockResponse = {
    MockResponse(
      mockId = request.mockId,
      name = request.name,
      statusCode = request.statusCode,
      requestHeaders = request.requestHeaders.toSet,
      responseHeaders = request.responseHeaders.toSet,
      queryParams = request.queryParams.toSet,
      pathParams = request.pathParams.toSet,
      response = request.responseContent
    )
  }

  def convertGetResponseRequest(request: GetResponse.Request): MockQuery = {
    MockQuery(
      rawUrl = request.rawUrl,
      servicePath = request.servicePath,
      requestPath = request.requestPath,
      method = request.method,
      body = request.body,
      headers = request.headers.distinct.sorted,
      queryParams = request.queryParams.distinct.sorted
    )
  }

}
