package com.mocker.rest.api

import com.mocker.rest.rest_service.{
  CreateMockRequest,
  CreateMockResponse,
  CreateServiceResponse,
  DeleteMockRequest,
  DeleteMockResponse,
  DeleteServiceRequest,
  DeleteServiceResponse,
  GetAllServicesRequest,
  GetAllServicesResponse,
  GetMockRequest,
  GetMockResponse,
  GetServiceMocksRequest,
  GetServiceMocksResponse,
  GetServiceRequest,
  GetServiceResponse,
  UpdateMockRequest,
  UpdateMockResponse,
  UpdateServiceRequest,
  UpdateServiceResponse
}
import com.mocker.rest.rest_service.ZioRestService.RestMocker
import io.grpc.Status
import zio.ZIO

class RestMockerService extends RestMocker {
  override def createService(request: CreateMockRequest): ZIO[Any, Status, CreateServiceResponse] = ???

  override def deleteService(request: DeleteServiceRequest): ZIO[Any, Status, DeleteServiceResponse] = ???

  override def updateService(request: UpdateServiceRequest): ZIO[Any, Status, UpdateServiceResponse] = ???

  override def getService(request: GetServiceRequest): ZIO[Any, Status, GetServiceResponse] = ???

  override def createMock(request: CreateMockRequest): ZIO[Any, Status, CreateMockResponse] = ???

  override def deleteMock(request: DeleteMockRequest): ZIO[Any, Status, DeleteMockResponse] = ???

  override def updateMock(request: UpdateMockRequest): ZIO[Any, Status, UpdateMockResponse] = ???

  override def getMock(request: GetMockRequest): ZIO[Any, Status, GetMockResponse] = ???

  override def getAllServices(request: GetAllServicesRequest): ZIO[Any, Status, GetAllServicesResponse] = ???

  override def getServiceMocks(request: GetServiceMocksRequest): ZIO[Any, Status, GetServiceMocksResponse] = ???
}
