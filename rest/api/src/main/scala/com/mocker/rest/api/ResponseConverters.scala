package com.mocker.rest.api

import com.google.protobuf.util.Timestamps
import com.google.protobuf.timestamp.{Timestamp => GoogleTimestamp}
import com.mocker.rest.mock.MockSnippet
import com.mocker.rest.mock_response.MockResponseSnippet
import com.mocker.rest.model.{Mock, MockResponse, Model, ModelSnippet, Service, ServiceStats}
import com.mocker.rest.rest_service._
import com.mocker.rest.service.ServiceSnippet
import com.mocker.rest.utils.PathUtils.buildFullPath

import java.sql.Timestamp

object ResponseConverters {

  def toGetServiceResponse(service: Service): GetService.Response = {
    GetService.Response(
      name = service.name,
      path = service.path,
      url = service.url,
      description = service.description,
      expirationTime = service.expirationTime.map(toProtoTimestamp),
      creationTime = Some(toProtoTimestamp(service.creationTime)),
      updateTime = Some(toProtoTimestamp(service.updateTime))
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
      name = model.name,
      description = model.description,
      schema = model.schema.toString
    )
  }

  def toGetAllServiceModelsResponse(models: Seq[Model]): GetAllServiceModels.Response = {
    GetAllServiceModels.Response(models.map(toModelSnippet))
  }

  def toGetMockResponse(mock: Mock): GetMock.Response = {
    GetMock.Response(
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
      mockId = mockResponse.mockId,
      name = mockResponse.name,
      statusCode = mockResponse.statusCode,
      requestHeaders = mockResponse.requestHeaders,
      responseHeaders = mockResponse.responseHeaders,
      queryParams = mockResponse.queryParams,
      pathParams = mockResponse.pathParams,
      responseContent = mockResponse.response.toString
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

  private def toProtoTimestamp(ts: Timestamp): GoogleTimestamp = {
    GoogleTimestamp.fromJavaProto(Timestamps.fromMillis(ts.getTime))
  }
}
