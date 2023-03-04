package com.mocker.rest.api

import com.mocker.rest.manager.RestMockerManager
import com.mocker.rest.rest_service._
import com.mocker.rest.rest_service.ZioRestService.RestMocker
import io.grpc.Status
import zio.{ZIO, ZLayer}

case class RestMockerService(restMockerManager: RestMockerManager) extends RestMocker {
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

object RestMockerService {

  def layer: ZLayer[RestMockerManager, Nothing, RestMockerService] = {
    ZLayer.fromZIO {
      for {
        restMockerManager <- ZIO.service[RestMockerManager]
      } yield RestMockerService(restMockerManager)
    }
  }
}
