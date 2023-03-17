package com.mocker.rest.api

import com.mocker.rest.api.RequestConverters._
import com.mocker.rest.api.ResponseConverters._
import com.mocker.rest.manager.RestMockerManager
import com.mocker.rest.rest_service.ZioRestService.RestMocker
import com.mocker.rest.rest_service._
import io.grpc.Status
import zio.{IO, URLayer, ZIO, ZLayer}

case class RestMockerService(restMockerManager: RestMockerManager) extends RestMocker {
  override def createService(request: CreateService.Request): IO[Status, CreateService.Response] = {
    restMockerManager
      .createService(convertCreateServiceRequest(request))
      .mapError(_.status)
      .map(_ => CreateService.Response())
  }

  override def createModel(request: CreateModel.Request): IO[Status, CreateModel.Response] = {
    restMockerManager
      .createModel(request.servicePath, convertCreateModelRequest(request))
      .mapError(_.status)
      .map(_ => CreateModel.Response())
  }

  override def createMock(request: CreateMock.Request): IO[Status, CreateMock.Response] = {
    restMockerManager
      .createMock(request.servicePath, convertCreateMockRequest(request))
      .mapError(_.status)
      .map(_ => CreateMock.Response())
  }

  override def createMockStaticResponse(
      request: CreateMockStaticResponse.Request
  ): IO[Status, CreateMockStaticResponse.Response] = {
    restMockerManager
      .createMockResponse(request.mockId, convertCreateMockResponseRequest(request))
      .mapError(_.status)
      .map(_ => CreateMockStaticResponse.Response())
  }

  override def getService(request: GetService.Request): IO[Status, GetService.Response] = {
    restMockerManager
      .getService(request.path)
      .mapError(_.status)
      .map(toGetServiceResponse)
  }

  override def getAllServices(request: GetAllServices.Request): IO[Status, GetAllServices.Response] = {
    restMockerManager.getServicesWithStats
      .mapError(_.status)
      .map(toGetAllServicesResponse)
  }

  override def searchServices(request: SearchServices.Request): IO[Status, SearchServices.Response] = {
    restMockerManager
      .searchServices(request.query)
      .mapError(_.status)
      .map(toSearchServicesResponse)
  }

  override def getModel(request: GetModel.Request): IO[Status, GetModel.Response] = {
    restMockerManager
      .getModel(request.servicePath, request.modelId)
      .mapError(_.status)
      .map(toGetModelResponse)
  }

  override def getAllServiceModels(
      request: GetAllServiceModels.Request
  ): ZIO[Any, Status, GetAllServiceModels.Response] = {
    restMockerManager
      .getAllServiceModels(request.servicePath)
      .mapError(_.status)
      .map(toGetAllServiceModelsResponse)
  }

  override def getMock(request: GetMock.Request): IO[Status, GetMock.Response] = {
    restMockerManager
      .getMock(request.servicePath, request.mockId)
      .mapError(_.status)
      .map(toGetMockResponse)
  }

  override def getAllServiceMocks(request: GetAllServiceMocks.Request): IO[Status, GetAllServiceMocks.Response] = {
    restMockerManager
      .getAllServiceMocks(request.servicePath)
      .mapError(_.status)
      .map(toGetAllServiceMocksResponse)
  }

  override def getMockStaticResponse(
      request: GetMockStaticResponse.Request
  ): IO[Status, GetMockStaticResponse.Response] = {
    restMockerManager
      .getMockResponse(request.servicePath, request.mockId, request.responseId)
      .mapError(_.status)
      .map(toGetMockStaticResponse)
  }

  override def getAllMockStaticResponses(
      request: GetAllMockStaticResponses.Request
  ): IO[Status, GetAllMockStaticResponses.Response] = {
    restMockerManager
      .getAllMockResponses(request.servicePath, request.mockId)
      .mapError(_.status)
      .map { case (mock, responses) => toGetAllMockStaticResponses(mock, responses) }
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
