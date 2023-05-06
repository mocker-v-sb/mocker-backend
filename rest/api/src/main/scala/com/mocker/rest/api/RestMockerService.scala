package com.mocker.rest.api

import com.mocker.common.paging.{Page, Paging}
import com.mocker.rest.api.RequestConverters._
import com.mocker.rest.api.ResponseConverters._
import com.mocker.rest.manager.{
  RestHistoryManager,
  RestMockManager,
  RestMockResponseManager,
  RestModelManager,
  RestResponseManager,
  RestServiceManager
}
import com.mocker.rest.rest_service.ZioRestService.RestMocker
import com.mocker.rest.rest_service._
import io.grpc.Status
import zio.{Console, IO, URLayer, ZIO, ZLayer}

case class RestMockerService(
    restServiceManager: RestServiceManager,
    restModelManager: RestModelManager,
    restMockManager: RestMockManager,
    restMockResponseManager: RestMockResponseManager,
    restResponseManager: RestResponseManager,
    restHistoryManager: RestHistoryManager
) extends RestMocker {
  override def createService(request: CreateService.Request): IO[Status, CreateService.Response] = {
    restServiceManager
      .createService(convertCreateServiceRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => CreateService.Response())
  }

  override def createModel(request: CreateModel.Request): IO[Status, CreateModel.Response] = {
    restModelManager
      .upsertModel(request.servicePath, convertCreateModelRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => CreateModel.Response())
  }

  override def createMock(request: CreateMock.Request): IO[Status, CreateMock.Response] = {
    restMockManager
      .createMock(request.servicePath, convertCreateMockRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => CreateMock.Response())
  }

  override def createMockStaticResponse(
      request: CreateMockStaticResponse.Request
  ): IO[Status, CreateMockStaticResponse.Response] = {
    restMockResponseManager
      .createMockResponse(request.servicePath, request.mockId, convertCreateMockResponseRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => CreateMockStaticResponse.Response())
  }

  override def getService(request: GetService.Request): IO[Status, GetService.Response] = {
    restServiceManager
      .getService(request.path)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetServiceResponse)
  }

  override def getServiceResponseHistory(
      request: GetServiceResponseHistory.Request
  ): IO[Status, GetServiceResponseHistory.Response] = {
    val pageNum = request.page.map(_.num).getOrElse(0)
    val pageSize = request.page.map(_.size).getOrElse(10)
    val shift = pageNum * pageSize
    val itemsZ = restHistoryManager
      .getServiceHistory(
        request.id,
        request.searchUrl,
        request.from.map(fromProtoTimestamp),
        request.to.map(fromProtoTimestamp),
        pageSize,
        shift
      )
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_.map(toGetServiceHistoryItem))
    val countZ = restHistoryManager
      .countHistoryItems(
        request.id,
        request.searchUrl,
        request.from.map(fromProtoTimestamp),
        request.to.map(fromProtoTimestamp)
      )
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
    for {
      result <- itemsZ.zipPar(countZ)
      (items, count) = result
    } yield GetServiceResponseHistory.Response(
      paging = Some(
        Paging(
          totalPages = Math.ceil(count.toDouble / pageSize).toInt,
          totalItems = count,
          page = Some(Page(num = pageNum, size = pageSize))
        )
      ),
      items = items
    )
  }

  override def getAllServices(request: GetAllServices.Request): IO[Status, GetAllServices.Response] = {
    restServiceManager.getServicesWithStats
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetAllServicesResponse)
  }

  override def searchServices(request: SearchServices.Request): IO[Status, SearchServices.Response] = {
    restServiceManager
      .searchServices(request.query)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toSearchServicesResponse)
  }

  override def switchServiceProxy(request: SwitchServiceProxy.Request): IO[Status, SwitchServiceProxy.Response] = {
    restServiceManager
      .switchServiceProxy(request.path, request.isProxyEnabled)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => SwitchServiceProxy.Response())
  }

  override def switchServiceHistory(
      request: SwitchServiceHistory.Request
  ): IO[Status, SwitchServiceHistory.Response] = {
    restServiceManager
      .switchServiceHistory(request.path, request.isHistoryEnabled)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => SwitchServiceHistory.Response())
  }

  override def getModel(request: GetModel.Request): IO[Status, GetModel.Response] = {
    restModelManager
      .getModel(request.servicePath, request.modelId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetModelResponse)
  }

  override def getAllServiceModels(
      request: GetAllServiceModels.Request
  ): IO[Status, GetAllServiceModels.Response] = {
    restModelManager
      .getAllServiceModels(request.servicePath)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetAllServiceModelsResponse)
  }

  override def getMock(request: GetMock.Request): IO[Status, GetMock.Response] = {
    restMockManager
      .getMock(request.servicePath, request.mockId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetMockResponse)
  }

  override def getAllServiceMocks(request: GetAllServiceMocks.Request): IO[Status, GetAllServiceMocks.Response] = {
    restMockManager
      .getAllServiceMocks(request.servicePath)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetAllServiceMocksResponse)
  }

  override def getMockStaticResponse(
      request: GetMockStaticResponse.Request
  ): IO[Status, GetMockStaticResponse.Response] = {
    restMockResponseManager
      .getMockResponse(request.servicePath, request.mockId, request.responseId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetMockStaticResponse)
  }

  override def getAllMockStaticResponses(
      request: GetAllMockStaticResponses.Request
  ): IO[Status, GetAllMockStaticResponses.Response] = {
    restMockResponseManager
      .getAllMockResponses(request.servicePath, request.mockId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map { case (mock, responses) => toGetAllMockStaticResponses(mock, responses) }
  }

  override def deleteService(request: DeleteService.Request): IO[Status, DeleteService.Response] = {
    restServiceManager
      .deleteService(request.servicePath)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteService.Response())
  }

  override def deleteModel(request: DeleteModel.Request): IO[Status, DeleteModel.Response] = {
    restModelManager
      .deleteModel(request.servicePath, request.modelId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteModel.Response())
  }

  override def deleteMock(request: DeleteMock.Request): IO[Status, DeleteMock.Response] = {
    restMockManager
      .deleteMock(request.servicePath, request.mockId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteMock.Response())
  }

  override def deleteMockStaticResponse(
      request: DeleteMockStaticResponse.Request
  ): IO[Status, DeleteMockStaticResponse.Response] = {
    restMockResponseManager
      .deleteMockStaticResponse(request.servicePath, request.mockId, request.responseId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteMockStaticResponse.Response())
  }

  override def deleteAllModels(request: DeleteAllModels.Request): IO[Status, DeleteAllModels.Response] = {
    restModelManager
      .deleteAllModels(request.servicePath)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteAllModels.Response())
  }

  override def deleteAllMocks(request: DeleteAllMocks.Request): IO[Status, DeleteAllMocks.Response] = {
    restMockManager
      .deleteAllMocks(request.servicePath)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteAllMocks.Response())
  }

  override def deleteAllMockStatisResponses(
      request: DeleteAllMockStaticResponses.Request
  ): IO[Status, DeleteAllMockStaticResponses.Response] = {
    restMockResponseManager
      .deleteAllMockStaticResponses(request.servicePath, request.mockId)
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => DeleteAllMockStaticResponses.Response())
  }

  override def updateService(request: UpdateService.Request): IO[Status, UpdateService.Response] = {
    restServiceManager
      .updateService(request.servicePath, convertUpdateServiceRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => UpdateService.Response())
  }

  override def updateModel(request: UpdateModel.Request): IO[Status, UpdateModel.Response] = {
    restModelManager
      .upsertModel(request.servicePath, convertUpdateModelRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => UpdateModel.Response())
  }

  override def updateMock(request: UpdateMock.Request): IO[Status, UpdateMock.Response] = {
    restMockManager
      .updateMock(request.servicePath, request.mockId, convertUpdateMockRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(_ => UpdateMock.Response())
  }

  override def updateMockStaticResponse(
      request: UpdateMockStaticResponse.Request
  ): IO[Status, UpdateMockStaticResponse.Response] = {
    restMockResponseManager
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
    restResponseManager
      .getMockResponse(convertGetResponseRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetResponse)
  }
}

object RestMockerService {

  def layer: URLayer[RestServiceManager with RestModelManager with RestMockManager with RestMockResponseManager with RestResponseManager with RestHistoryManager, RestMockerService] = {
    ZLayer.fromZIO {
      for {
        restServiceManager <- ZIO.service[RestServiceManager]
        restModelManager <- ZIO.service[RestModelManager]
        restMockManager <- ZIO.service[RestMockManager]
        restMockResponseManager <- ZIO.service[RestMockResponseManager]
        restHistoryManager <- ZIO.service[RestHistoryManager]
        restResponseManager <- ZIO.service[RestResponseManager]
      } yield RestMockerService(
        restServiceManager,
        restModelManager,
        restMockManager,
        restMockResponseManager,
        restResponseManager,
        restHistoryManager
      )
    }
  }
}
