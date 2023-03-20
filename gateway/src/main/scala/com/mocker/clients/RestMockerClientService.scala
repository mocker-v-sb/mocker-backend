package com.mocker.clients

import com.mocker.models.rest.requests.mock.{CreateMockRequest, DeleteAllServiceMocksRequest, DeleteMockRequest, GetAllServiceMocksRequest, GetMockRequest, UpdateMockRequest}
import com.mocker.models.rest.requests.model._
import com.mocker.models.rest.requests.service._
import com.mocker.rest.rest_service
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

  def getAllServices: Response[GetAllServices.Response] = {
    RestMockerClient.getAllServices(GetAllServices.Request())
  }

  def searchServices(request: SearchServicesRequest): Response[SearchServices.Response] = {
    RestMockerClient.searchServices(request.toMessage)
  }

  def updateService(request: UpdateServiceRequest): Response[UpdateService.Response] = {
    RestMockerClient.updateService(request.toMessage)
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

}
