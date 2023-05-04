package com.mocker.clients

import com.mocker.models.rest.requests.{service, GetResponseRequest}
import com.mocker.models.rest.requests.mock._
import com.mocker.models.rest.requests.mock_response._
import com.mocker.models.rest.requests.model._
import com.mocker.models.rest.requests.service._
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import com.mocker.rest.rest_service._
import io.grpc.Status
import zio.ZIO

object RestMockerClientService {

  type Response[R] = ZIO[RestMockerClient.Service, Status, R]

  def createService(request: CreateServiceRequest): Response[CreateService.Response] = {
    RestMockerClient.createService(request.toMessage)
  }

  def getService(request: GetServiceRequest): Response[GetService.Response] = {
    RestMockerClient.getService(request.toMessage)
  }

  def getServiceResponseHistory(
      request: GetServiceResponseHistoryRequest
  ): Response[GetServiceResponseHistory.Response] = {
    RestMockerClient.getServiceResponseHistory(request.toMessage)
  }

  def getAllServices: Response[GetAllServices.Response] = {
    RestMockerClient.getAllServices(GetAllServices.Request())
  }

  def searchServices(request: SearchServicesRequest): Response[SearchServices.Response] = {
    RestMockerClient.searchServices(request.toMessage)
  }

  def updateService(request: UpdateServiceRequest): Response[UpdateService.Response] = {
    RestMockerClient.updateService(request.toMessage)
  }

  def switchServiceProxy(request: SwitchServiceProxyRequest): Response[SwitchServiceProxy.Response] = {
    RestMockerClient.switchServiceProxy(request.toMessage)
  }

  def switchServiceHistory(request: SwitchServiceHistoryRequest): Response[SwitchServiceHistory.Response] = {
    RestMockerClient.switchServiceHistory(request.toMessage)
  }

  def deleteService(request: DeleteServiceRequest): Response[DeleteService.Response] = {
    RestMockerClient.deleteService(request.toMessage)
  }

  def createModel(request: CreateModelRequest): Response[CreateModel.Response] = {
    RestMockerClient.createModel(request.toMessage)
  }

  def getModel(request: GetModelRequest): Response[GetModel.Response] = {
    RestMockerClient.getModel(request.toMessage)
  }

  def getAllServiceModels(request: GetAllServiceModelsRequest): Response[GetAllServiceModels.Response] = {
    RestMockerClient.getAllServiceModels(request.toMessage)
  }

  def updateModel(request: UpdateModelRequest): Response[UpdateModel.Response] = {
    RestMockerClient.updateModel(request.toMessage)
  }

  def deleteModel(request: DeleteModelRequest): Response[DeleteModel.Response] = {
    RestMockerClient.deleteModel(request.toMessage)
  }

  def deleteServiceModels(request: DeleteAllServiceModelsRequest): Response[DeleteAllModels.Response] = {
    RestMockerClient.deleteAllModels(request.toMessage)
  }

  def createMock(request: CreateMockRequest): Response[CreateMock.Response] = {
    RestMockerClient.createMock(request.toMessage)
  }

  def getMock(request: GetMockRequest): Response[GetMock.Response] = {
    RestMockerClient.getMock(request.toMessage)
  }

  def getAllServiceMocks(request: GetAllServiceMocksRequest): Response[GetAllServiceMocks.Response] = {
    RestMockerClient.getAllServiceMocks(request.toMessage)
  }

  def updateMock(request: UpdateMockRequest): Response[UpdateMock.Response] = {
    RestMockerClient.updateMock(request.toMessage)
  }

  def deleteMock(request: DeleteMockRequest): Response[DeleteMock.Response] = {
    RestMockerClient.deleteMock(request.toMessage)
  }

  def deleteServiceMocks(request: DeleteAllServiceMocksRequest): Response[DeleteAllMocks.Response] = {
    RestMockerClient.deleteAllMocks(request.toMessage)
  }

  def createMockStaticResponse(
      request: CreateMockStaticResponseRequest
  ): Response[CreateMockStaticResponse.Response] = {
    RestMockerClient.createMockStaticResponse(request.toMessage)
  }

  def getMockStaticResponse(request: GetMockStaticResponseRequest): Response[GetMockStaticResponse.Response] = {
    RestMockerClient.getMockStaticResponse(request.toMessage)
  }

  def getAllMockStaticResponses(
      request: GetAllMockStaticResponsesRequest
  ): Response[GetAllMockStaticResponses.Response] = {
    RestMockerClient.getAllMockStaticResponses(request.toMessage)
  }

  def updateMockStaticResponse(
      request: UpdateMockStaticResponseRequest
  ): Response[UpdateMockStaticResponse.Response] = {
    RestMockerClient.updateMockStaticResponse(request.toMessage)
  }

  def deleteMockStaticResponse(
      request: DeleteMockStaticResponseRequest
  ): Response[DeleteMockStaticResponse.Response] = {
    RestMockerClient.deleteMockStaticResponse(request.toMessage)
  }

  def deleteAllMockStaticResponses(
      request: DeleteAllMockStaticResponsesRequest
  ): Response[DeleteAllMockStaticResponses.Response] = {
    RestMockerClient.deleteAllMockStatisResponses(request.toMessage)
  }

  def getResponse(request: GetResponseRequest): Response[GetResponse.Response] = {
    RestMockerClient.getResponse(request.toMessage)
  }

}
