package com.mocker.rest.api

import com.google.protobuf.timestamp.{Timestamp => GoogleTimestamp}
import com.google.protobuf.util.Timestamps
import com.mocker.rest.api.CommonConverters.convertModelResponse
import com.mocker.rest.mock.MockSnippet
import com.mocker.rest.mock_history.{HistoryItem => ProtoHistoryItem}
import com.mocker.rest.mock_response.MockResponseSnippet
import com.mocker.rest.model._
import com.mocker.rest.rest_service._
import com.mocker.rest.service.ServiceSnippet
import com.mocker.rest.utils.PathUtils.buildFullPath

import java.time.Instant

object ResponseConverters {

  def toGetServiceResponse(service: Service): GetService.Response = {
    GetService.Response(
      name = service.name,
      path = service.path,
      url = service.url,
      description = service.description,
      expirationTime = service.expirationTime.map(toProtoTimestamp),
      creationTime = Some(toProtoTimestamp(service.creationTime)),
      updateTime = Some(toProtoTimestamp(service.updateTime)),
      isProxyEnabled = service.isProxyEnabled,
      id = service.id,
      isHistoryEnabled = service.isHistoryEnabled
    )
  }

  def toGetServiceHistoryItem(item: MockHistoryItem): ProtoHistoryItem = {
    ProtoHistoryItem(
      id = item.id,
      method = item.method,
      queryUrl = item.queryUrl,
      responseUrl = item.responseUrl,
      responseSource = item.responseSource,
      statusCode = item.statusCode,
      requestHeaders = item.requestHeaders,
      responseHeaders = item.responseHeaders,
      responseTime = Some(toProtoTimestamp(item.responseTime)),
      response = item.response
    )
  }

  def toGetAllServicesResponse(stats: Seq[ServiceStats]): GetAllServices.Response = {
    GetAllServices.Response(stats.map(toServiceSnippet))
  }

  def toSearchServicesResponse(stats: Seq[ServiceStats]): SearchServices.Response = {
    SearchServices.Response(stats.map(toServiceSnippet))
  }

  def toGetModelResponse(model: Model): GetModel.Response = {
    GetModel.Response(
      id = model.id,
      name = model.name,
      description = model.description,
      responseType = model.responseType,
      response = convertModelResponse(model.responseType, model.response)
    )
  }

  def toGetAllServiceModelsResponse(models: Seq[Model]): GetAllServiceModels.Response = {
    GetAllServiceModels.Response(models.map(toModelSnippet))
  }

  def toGetMockResponse(mock: Mock): GetMock.Response = {
    GetMock.Response(
      id = mock.id,
      name = mock.name,
      description = mock.description,
      path = mock.path,
      method = mock.method,
      requestModelId = mock.requestModelId,
      responseModelId = mock.responseModelId,
      requestHeaders = mock.requestHeaders,
      responseHeaders = mock.responseHeaders,
      queryParams = mock.queryParams,
      pathParams = mock.pathParams
    )
  }

  def toGetAllServiceMocksResponse(mocks: Seq[Mock]): GetAllServiceMocks.Response = {
    GetAllServiceMocks.Response(mocks.map(toMockSnippet))
  }

  def toGetMockStaticResponse(mockResponse: MockResponse): GetMockStaticResponse.Response = {
    GetMockStaticResponse.Response(
      id = mockResponse.id,
      mockId = mockResponse.mockId,
      name = mockResponse.name,
      statusCode = mockResponse.statusCode,
      requestHeaders = mockResponse.requestHeaders.toSeq,
      responseHeaders = mockResponse.responseHeaders.toSeq,
      queryParams = mockResponse.queryParams.toSeq,
      pathParams = mockResponse.pathParams.toSeq,
      responseContent = mockResponse.response
    )
  }

  def toGetAllMockStaticResponses(mock: Mock, mockResponses: Seq[MockResponse]): GetAllMockStaticResponses.Response = {
    GetAllMockStaticResponses.Response(mockResponses.map(r => toMockResponseSnippet(mock, r)))
  }

  def toMockResponseSnippet(mock: Mock, mockResponse: MockResponse): MockResponseSnippet = {
    MockResponseSnippet(
      responseId = mockResponse.id,
      name = mockResponse.name,
      statusCode = mockResponse.statusCode,
      fullPath = buildFullPath(mock, mockResponse)
    )
  }

  def toGetResponse(queryResponse: MockQueryResponse): GetResponse.Response = {
    GetResponse.Response(
      statusCode = queryResponse.statusCode,
      content = queryResponse.content,
      headers = queryResponse.headers
    )
  }

  private def toMockSnippet(mock: Mock): MockSnippet = {
    MockSnippet(
      mockId = mock.id,
      name = mock.name,
      description = mock.description,
      path = mock.path,
      method = mock.method
    )
  }

  private def toModelSnippet(model: Model): ModelSnippet = {
    ModelSnippet(modelId = model.id, name = model.name, description = model.description)
  }

  private def toServiceSnippet(stat: ServiceStats): ServiceSnippet = {
    ServiceSnippet(
      name = stat.service.name,
      path = stat.service.path,
      url = stat.service.url,
      totalMocks = stat.totalMocks,
      totalModels = stat.totalModels
    )
  }

  private def toProtoTimestamp(instant: Instant): GoogleTimestamp = {
    GoogleTimestamp.fromJavaProto(Timestamps.fromMillis(instant.toEpochMilli))
  }
}
