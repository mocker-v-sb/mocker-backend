package com.mocker.rest.api

import com.mocker.rest.api.Converters._
import com.mocker.rest.manager.RestMockerManager
import com.mocker.rest.rest_service.ZioRestService.RestMocker
import com.mocker.rest.rest_service._
import io.grpc.Status
import zio.{ZIO, ZLayer}

case class RestMockerService(restMockerManager: RestMockerManager) extends RestMocker {
  override def createService(request: CreateServiceRequest): ZIO[Any, Status, CreateServiceResponse] = {
    restMockerManager
      .createService(convertCreateServiceRequest(request))
      .mapError(Status.fromThrowable)
      .map(_ => CreateServiceResponse())
  }

  override def createModel(request: CreateModelRequest): ZIO[Any, Status, CreateModelResponse] = {
    restMockerManager
      .createModel(request.servicePath, convertCreateModelRequest(request))
      .mapError(Status.fromThrowable)
      .map(_ => CreateModelResponse())
  }

  override def createMock(request: CreateMockRequest): ZIO[Any, Status, CreateMockResponse] = {
    restMockerManager
      .createMock(request.servicePath, convertCreateMockRequest(request))
      .mapError(Status.fromThrowable)
      .map(_ => CreateMockResponse())
  }
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
