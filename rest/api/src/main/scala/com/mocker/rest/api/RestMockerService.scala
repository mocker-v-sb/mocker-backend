package com.mocker.rest.api

import com.mocker.rest.api.RequestConverters._
import com.mocker.rest.api.ResponseConverters._
import com.mocker.rest.manager.RestMockerManager
import com.mocker.rest.rest_service
import com.mocker.rest.rest_service.ZioRestService.RestMocker
import com.mocker.rest.rest_service._
import io.grpc.Status
import zio.{Console, IO, URLayer, ZIO, ZLayer}

case class RestMockerService(restMockerManager: RestMockerManager) extends RestMocker {
  override def createService(request: CreateService.Request): IO[Status, CreateService.Response] = {
    restMockerManager
      .createService(convertCreateServiceRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => CreateService.Response())
  }

  override def createModel(request: CreateModel.Request): IO[Status, CreateModel.Response] = {
    restMockerManager
      .upsertModel(request.servicePath, convertCreateModelRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => CreateModel.Response())
  }

  override def createMock(request: CreateMock.Request): IO[Status, CreateMock.Response] = {
    restMockerManager
      .createMock(request.servicePath, convertCreateMockRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => CreateMock.Response())
  }

  override def createMockStaticResponse(
      request: CreateMockStaticResponse.Request
  ): IO[Status, CreateMockStaticResponse.Response] = {
    restMockerManager
      .createMockResponse(request.servicePath, request.mockId, convertCreateMockResponseRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => CreateMockStaticResponse.Response())
  }

  override def getService(request: GetService.Request): IO[Status, GetService.Response] = {
    restMockerManager
      .getService(request.path)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetServiceResponse)
  }

  override def getAllServices(request: GetAllServices.Request): IO[Status, GetAllServices.Response] = {
    restMockerManager.getServicesWithStats
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetAllServicesResponse)
  }

  override def searchServices(request: SearchServices.Request): IO[Status, SearchServices.Response] = {
    restMockerManager
      .searchServices(request.query)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toSearchServicesResponse)
  }

  override def switchServiceProxy(request: SwitchServiceProxy.Request): IO[Status, SwitchServiceProxy.Response] = {
    restMockerManager
      .switchServiceProxy(request.path, request.isProxyEnabled)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => SwitchServiceProxy.Response())
  }

  override def switchServiceHistory(
      request: SwitchServiceHistory.Request
  ): IO[Status, SwitchServiceHistory.Response] = {
    restMockerManager
      .switchServiceHistory(request.path, request.isHistoryEnabled)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => SwitchServiceHistory.Response())
  }

  override def getModel(request: GetModel.Request): IO[Status, GetModel.Response] = {
    restMockerManager
      .getModel(request.servicePath, request.modelId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetModelResponse)
  }

  override def getAllServiceModels(
      request: GetAllServiceModels.Request
  ): IO[Status, GetAllServiceModels.Response] = {
    restMockerManager
      .getAllServiceModels(request.servicePath)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetAllServiceModelsResponse)
  }

  override def getMock(request: GetMock.Request): IO[Status, GetMock.Response] = {
    restMockerManager
      .getMock(request.servicePath, request.mockId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetMockResponse)
  }

  override def getAllServiceMocks(request: GetAllServiceMocks.Request): IO[Status, GetAllServiceMocks.Response] = {
    restMockerManager
      .getAllServiceMocks(request.servicePath)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetAllServiceMocksResponse)
  }

  override def getMockStaticResponse(
      request: GetMockStaticResponse.Request
  ): IO[Status, GetMockStaticResponse.Response] = {
    restMockerManager
      .getMockResponse(request.servicePath, request.mockId, request.responseId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetMockStaticResponse)
  }

  override def getAllMockStaticResponses(
      request: GetAllMockStaticResponses.Request
  ): IO[Status, GetAllMockStaticResponses.Response] = {
    restMockerManager
      .getAllMockResponses(request.servicePath, request.mockId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map { case (mock, responses) => toGetAllMockStaticResponses(mock, responses) }
  }

  override def deleteService(request: DeleteService.Request): IO[Status, DeleteService.Response] = {
    restMockerManager
      .deleteService(request.servicePath)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteService.Response())
  }

  override def deleteModel(request: DeleteModel.Request): IO[Status, DeleteModel.Response] = {
    restMockerManager
      .deleteModel(request.servicePath, request.modelId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteModel.Response())
  }

  override def deleteMock(request: DeleteMock.Request): IO[Status, DeleteMock.Response] = {
    restMockerManager
      .deleteMock(request.servicePath, request.mockId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteMock.Response())
  }

  override def deleteMockStaticResponse(
      request: DeleteMockStaticResponse.Request
  ): IO[Status, DeleteMockStaticResponse.Response] = {
    restMockerManager
      .deleteMockStaticResponse(request.servicePath, request.mockId, request.responseId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteMockStaticResponse.Response())
  }

  override def deleteAllModels(request: DeleteAllModels.Request): IO[Status, DeleteAllModels.Response] = {
    restMockerManager
      .deleteAllModels(request.servicePath)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteAllModels.Response())
  }

  override def deleteAllMocks(request: DeleteAllMocks.Request): IO[Status, DeleteAllMocks.Response] = {
    restMockerManager
      .deleteAllMocks(request.servicePath)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteAllMocks.Response())
  }

  override def deleteAllMockStatisResponses(
      request: DeleteAllMockStaticResponses.Request
  ): IO[Status, DeleteAllMockStaticResponses.Response] = {
    restMockerManager
      .deleteAllMockStaticResponses(request.servicePath, request.mockId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteAllMockStaticResponses.Response())
  }

  override def updateService(request: UpdateService.Request): IO[Status, UpdateService.Response] = {
    restMockerManager
      .updateService(request.servicePath, convertUpdateServiceRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => UpdateService.Response())
  }

  override def updateModel(request: UpdateModel.Request): IO[Status, UpdateModel.Response] = {
    restMockerManager
      .upsertModel(request.servicePath, convertUpdateModelRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => UpdateModel.Response())
  }

  override def updateMock(request: UpdateMock.Request): IO[Status, UpdateMock.Response] = {
    restMockerManager
      .updateMock(request.servicePath, request.mockId, convertUpdateMockRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => UpdateMock.Response())
  }

  override def updateMockStaticResponse(
      request: UpdateMockStaticResponse.Request
  ): IO[Status, UpdateMockStaticResponse.Response] = {
    restMockerManager
      .updateMockStaticResponse(
        request.servicePath,
        request.mockId,
        request.responseId,
        convertUpdateMockResponseRequest(request)
      )
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => UpdateMockStaticResponse.Response())
  }

  override def getResponse(request: GetResponse.Request): IO[Status, GetResponse.Response] = {
    restMockerManager
      .getMockResponse(convertGetResponseRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetResponse)
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
