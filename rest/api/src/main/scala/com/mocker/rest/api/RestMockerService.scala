package com.mocker.rest.api

import com.mocker.rest.api.Converters._
import com.mocker.rest.manager.RestMockerManager
import com.mocker.rest.rest_service.ZioRestService.RestMocker
import com.mocker.rest.rest_service._
import io.grpc.Status
import zio.{IO, URLayer, ZIO, ZLayer}

case class RestMockerService(restMockerManager: RestMockerManager) extends RestMocker {
  override def createService(request: CreateServiceRequest): IO[Status, CreateServiceResponse] = {
    restMockerManager
      .createService(convertCreateServiceRequest(request))
      .mapError(_.status)
      .map(_ => CreateServiceResponse())
  }

  override def createModel(request: CreateModelRequest): IO[Status, CreateModelResponse] = {
    restMockerManager
      .createModel(request.servicePath, convertCreateModelRequest(request))
      .mapError(_.status)
      .map(_ => CreateModelResponse())
  }

  override def createMock(request: CreateMockRequest): IO[Status, CreateMockResponse] = {
    restMockerManager
      .createMock(request.servicePath, convertCreateMockRequest(request))
      .mapError(_.status)
      .map(_ => CreateMockResponse())
  }

  override def createMockStaticResponse(
      request: CreateMockStaticResponseRequest
  ): IO[Status, CreateMockStaticResponseResponse] = {
    restMockerManager
      .createMockResponse(request.mockId, convertCreateMockResponseRequest(request))
      .mapError(_.status)
      .map(_ => CreateMockStaticResponseResponse())
  }
}

object RestMockerService {

  def layer: URLayer[RestMockerManager, RestMockerService] = {
    ZLayer.fromZIO {
      for {
        restMockerManager <- ZIO.service[RestMockerManager]
      } yield RestMockerService(restMockerManager)
    }
  }
}
