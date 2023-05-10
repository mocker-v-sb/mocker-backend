package com.mocker.clients

import com.mocker.common.auth.Authorization
import com.mocker.models.rest.requests.GetResponseRequest
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

  def createService(request: CreateServiceRequest)(auth: Authorization): Response[CreateService.Response] = {
    RestMockerClient.createService(request.toMessage.copy(auth = Some(auth)))
  }

  def getService(request: GetServiceRequest)(auth: Authorization): Response[GetService.Response] = {
    RestMockerClient.getService(request.toMessage.copy(auth = Some(auth)))
  }

  def getServiceResponseHistory(
      request: GetServiceResponseHistoryRequest
  )(auth: Authorization): Response[GetServiceResponseHistory.Response] = {
    RestMockerClient.getServiceResponseHistory(request.toMessage.copy(auth = Some(auth)))
  }

  def getAllServices(auth: Authorization): Response[GetAllServices.Response] = {
    RestMockerClient.getAllServices(GetAllServices.Request(auth = Some(auth)))
  }

  def searchServices(request: SearchServicesRequest)(auth: Authorization): Response[SearchServices.Response] = {
    RestMockerClient.searchServices(request.toMessage.copy(auth = Some(auth)))
  }

  def updateService(request: UpdateServiceRequest)(auth: Authorization): Response[UpdateService.Response] = {
    RestMockerClient.updateService(request.toMessage.copy(auth = Some(auth)))
  }

  def switchServiceProxy(
      request: SwitchServiceProxyRequest
  )(auth: Authorization): Response[SwitchServiceProxy.Response] = {
    RestMockerClient.switchServiceProxy(request.toMessage.copy(auth = Some(auth)))
  }

  def switchServiceHistory(
      request: SwitchServiceHistoryRequest
  )(auth: Authorization): Response[SwitchServiceHistory.Response] = {
    RestMockerClient.switchServiceHistory(request.toMessage.copy(auth = Some(auth)))
  }

  def deleteService(request: DeleteServiceRequest)(auth: Authorization): Response[DeleteService.Response] = {
    RestMockerClient.deleteService(request.toMessage.copy(auth = Some(auth)))
  }

  def createModel(request: CreateModelRequest)(auth: Authorization): Response[CreateModel.Response] = {
    RestMockerClient.createModel(request.toMessage.copy(auth = Some(auth)))
  }

  def getModel(request: GetModelRequest)(auth: Authorization): Response[GetModel.Response] = {
    RestMockerClient.getModel(request.toMessage.copy(auth = Some(auth)))
  }

  def getAllServiceModels(
      request: GetAllServiceModelsRequest
  )(auth: Authorization): Response[GetAllServiceModels.Response] = {
    RestMockerClient.getAllServiceModels(request.toMessage.copy(auth = Some(auth)))
  }

  def updateModel(request: UpdateModelRequest)(auth: Authorization): Response[UpdateModel.Response] = {
    RestMockerClient.updateModel(request.toMessage.copy(auth = Some(auth)))
  }

  def deleteModel(request: DeleteModelRequest)(auth: Authorization): Response[DeleteModel.Response] = {
    RestMockerClient.deleteModel(request.toMessage.copy(auth = Some(auth)))
  }

  def deleteServiceModels(
      request: DeleteAllServiceModelsRequest
  )(auth: Authorization): Response[DeleteAllModels.Response] = {
    RestMockerClient.deleteAllModels(request.toMessage.copy(auth = Some(auth)))
  }

  def createMock(request: CreateMockRequest)(auth: Authorization): Response[CreateMock.Response] = {
    RestMockerClient.createMock(request.toMessage.copy(auth = Some(auth)))
  }

  def getMock(request: GetMockRequest)(auth: Authorization): Response[GetMock.Response] = {
    RestMockerClient.getMock(request.toMessage.copy(auth = Some(auth)))
  }

  def getAllServiceMocks(
      request: GetAllServiceMocksRequest
  )(auth: Authorization): Response[GetAllServiceMocks.Response] = {
    RestMockerClient.getAllServiceMocks(request.toMessage.copy(auth = Some(auth)))
  }

  def updateMock(request: UpdateMockRequest)(auth: Authorization): Response[UpdateMock.Response] = {
    RestMockerClient.updateMock(request.toMessage.copy(auth = Some(auth)))
  }

  def deleteMock(request: DeleteMockRequest)(auth: Authorization): Response[DeleteMock.Response] = {
    RestMockerClient.deleteMock(request.toMessage.copy(auth = Some(auth)))
  }

  def deleteServiceMocks(
      request: DeleteAllServiceMocksRequest
  )(auth: Authorization): Response[DeleteAllMocks.Response] = {
    RestMockerClient.deleteAllMocks(request.toMessage.copy(auth = Some(auth)))
  }

  def createMockStaticResponse(
      request: CreateMockStaticResponseRequest
  )(auth: Authorization): Response[CreateMockStaticResponse.Response] = {
    RestMockerClient.createMockStaticResponse(request.toMessage.copy(auth = Some(auth)))
  }

  def getMockStaticResponse(
      request: GetMockStaticResponseRequest
  )(auth: Authorization): Response[GetMockStaticResponse.Response] = {
    RestMockerClient.getMockStaticResponse(request.toMessage.copy(auth = Some(auth)))
  }

  def getAllMockStaticResponses(
      request: GetAllMockStaticResponsesRequest
  )(auth: Authorization): Response[GetAllMockStaticResponses.Response] = {
    RestMockerClient.getAllMockStaticResponses(request.toMessage.copy(auth = Some(auth)))
  }

  def updateMockStaticResponse(
      request: UpdateMockStaticResponseRequest
  )(auth: Authorization): Response[UpdateMockStaticResponse.Response] = {
    RestMockerClient.updateMockStaticResponse(request.toMessage.copy(auth = Some(auth)))
  }

  def deleteMockStaticResponse(
      request: DeleteMockStaticResponseRequest
  )(auth: Authorization): Response[DeleteMockStaticResponse.Response] = {
    RestMockerClient.deleteMockStaticResponse(request.toMessage.copy(auth = Some(auth)))
  }

  def deleteAllMockStaticResponses(
      request: DeleteAllMockStaticResponsesRequest
  )(auth: Authorization): Response[DeleteAllMockStaticResponses.Response] = {
    RestMockerClient.deleteAllMockStatisResponses(request.toMessage.copy(auth = Some(auth)))
  }

  def getResponse(request: GetResponseRequest): Response[GetResponse.Response] = {
    RestMockerClient.getResponse(request.toMessage)
  }

}
