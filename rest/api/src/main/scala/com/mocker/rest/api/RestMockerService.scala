package com.mocker.rest.api

import com.mocker.common.auth.Authorization
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
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restServiceManager
        .createService(user, convertCreateServiceRequest(request))
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => CreateService.Response())
    } yield response
  }

  override def checkServiceExistence(
      request: CheckServiceExistence.Request
  ): IO[Status, CheckServiceExistence.Response] = {
    restServiceManager
      .getService(request.path)
      .mapError(_.status)
      .map(_ => CheckServiceExistence.Response())
  }

  override def createModel(request: CreateModel.Request): IO[Status, CreateModel.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restModelManager
        .upsertModel(user, request.servicePath, convertCreateModelRequest(request))
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => CreateModel.Response())
    } yield response
  }

  override def createMock(request: CreateMock.Request): IO[Status, CreateMock.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockManager
        .createMock(user, request.servicePath, convertCreateMockRequest(request))
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => CreateMock.Response())
    } yield response
  }

  override def createMockStaticResponse(
      request: CreateMockStaticResponse.Request
  ): IO[Status, CreateMockStaticResponse.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockResponseManager
        .createMockResponse(user, request.servicePath, request.mockId, convertCreateMockResponseRequest(request))
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => CreateMockStaticResponse.Response())
    } yield response
  }

  override def getService(request: GetService.Request): IO[Status, GetService.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restServiceManager
        .getService(user, request.path)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(toGetServiceResponse)
    } yield response
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
        request.from.map(_.asJavaInstant),
        request.to.map(_.asJavaInstant),
        request.statusCodes.toSet,
        request.responseSources.toSet,
        request.requestMethods.toSet,
        request.responseTimeSort,
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
        request.from.map(_.asJavaInstant),
        request.to.map(_.asJavaInstant),
        request.statusCodes.toSet,
        request.responseSources.toSet,
        request.requestMethods.toSet
      )
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
    for {
      user <- checkUserAuthorization(request.auth)
      _ <- restServiceManager.getService(user, request.id).mapError(_.status)
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
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restServiceManager
        .getServicesWithStats(user)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(toGetAllServicesResponse)
    } yield response
  }

  override def searchServices(request: SearchServices.Request): IO[Status, SearchServices.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restServiceManager
        .searchServices(user, request.query)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(toSearchServicesResponse)
    } yield response
  }

  override def switchServiceProxy(request: SwitchServiceProxy.Request): IO[Status, SwitchServiceProxy.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restServiceManager
        .switchServiceProxy(user, request.path, request.isProxyEnabled)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => SwitchServiceProxy.Response())
    } yield response
  }

  override def switchServiceHistory(
      request: SwitchServiceHistory.Request
  ): IO[Status, SwitchServiceHistory.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restServiceManager
        .switchServiceHistory(user, request.path, request.isHistoryEnabled)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => SwitchServiceHistory.Response())
    } yield response
  }

  override def getModel(request: GetModel.Request): IO[Status, GetModel.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restModelManager
        .getModel(user, request.servicePath, request.modelId)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(toGetModelResponse)
    } yield response
  }

  override def getAllServiceModels(
      request: GetAllServiceModels.Request
  ): IO[Status, GetAllServiceModels.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restModelManager
        .getAllServiceModels(user, request.servicePath)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(toGetAllServiceModelsResponse)
    } yield response
  }

  override def getMock(request: GetMock.Request): IO[Status, GetMock.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockManager
        .getMock(user, request.servicePath, request.mockId)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(toGetMockResponse)
    } yield response
  }

  override def getAllServiceMocks(request: GetAllServiceMocks.Request): IO[Status, GetAllServiceMocks.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockManager
        .getAllServiceMocks(user, request.servicePath)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(toGetAllServiceMocksResponse)
    } yield response
  }

  override def getMockStaticResponse(
      request: GetMockStaticResponse.Request
  ): IO[Status, GetMockStaticResponse.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockResponseManager
        .getMockResponse(user, request.servicePath, request.mockId, request.responseId)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(toGetMockStaticResponse)
    } yield response
  }

  override def getAllMockStaticResponses(
      request: GetAllMockStaticResponses.Request
  ): IO[Status, GetAllMockStaticResponses.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockResponseManager
        .getAllMockResponses(user, request.servicePath, request.mockId)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map { case (mock, responses) => toGetAllMockStaticResponses(mock, responses) }
    } yield response
  }

  override def deleteService(request: DeleteService.Request): IO[Status, DeleteService.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restServiceManager
        .deleteService(user, request.servicePath)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => DeleteService.Response())
    } yield response
  }

  override def deleteModel(request: DeleteModel.Request): IO[Status, DeleteModel.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restModelManager
        .deleteModel(user, request.servicePath, request.modelId)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => DeleteModel.Response())
    } yield response
  }

  override def deleteMock(request: DeleteMock.Request): IO[Status, DeleteMock.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockManager
        .deleteMock(user, request.servicePath, request.mockId)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => DeleteMock.Response())
    } yield response

  }

  override def deleteMockStaticResponse(
      request: DeleteMockStaticResponse.Request
  ): IO[Status, DeleteMockStaticResponse.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockResponseManager
        .deleteMockStaticResponse(user, request.servicePath, request.mockId, request.responseId)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => DeleteMockStaticResponse.Response())
    } yield response
  }

  override def deleteAllModels(request: DeleteAllModels.Request): IO[Status, DeleteAllModels.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restModelManager
        .deleteAllModels(user, request.servicePath)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => DeleteAllModels.Response())
    } yield response
  }

  override def deleteAllMocks(request: DeleteAllMocks.Request): IO[Status, DeleteAllMocks.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockManager
        .deleteAllMocks(user, request.servicePath)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => DeleteAllMocks.Response())
    } yield response
  }

  override def deleteAllMockStatisResponses(
      request: DeleteAllMockStaticResponses.Request
  ): IO[Status, DeleteAllMockStaticResponses.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockResponseManager
        .deleteAllMockStaticResponses(user, request.servicePath, request.mockId)
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => DeleteAllMockStaticResponses.Response())
    } yield response
  }

  override def updateService(request: UpdateService.Request): IO[Status, UpdateService.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restServiceManager
        .updateService(user, request.servicePath, convertUpdateServiceRequest(request))
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => UpdateService.Response())
    } yield response
  }

  override def updateModel(request: UpdateModel.Request): IO[Status, UpdateModel.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restModelManager
        .upsertModel(user, request.servicePath, convertUpdateModelRequest(request))
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => UpdateModel.Response())
    } yield response
  }

  override def updateMock(request: UpdateMock.Request): IO[Status, UpdateMock.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockManager
        .updateMock(user, request.servicePath, request.mockId, convertUpdateMockRequest(request))
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => UpdateMock.Response())
    } yield response
  }

  override def updateMockStaticResponse(
      request: UpdateMockStaticResponse.Request
  ): IO[Status, UpdateMockStaticResponse.Response] = {
    for {
      user <- checkUserAuthorization(request.auth)
      response <- restMockResponseManager
        .updateMockStaticResponse(
          user,
          request.servicePath,
          request.mockId,
          request.responseId,
          convertUpdateMockResponseRequest(request)
        )
        .tapError(err => Console.printLineError(err.message).ignoreLogged)
        .mapError(_.status)
        .map(_ => UpdateMockStaticResponse.Response())
    } yield response
  }

  override def getResponse(request: GetResponse.Request): IO[Status, GetResponse.Response] = {
    restResponseManager
      .getMockResponse(convertGetResponseRequest(request))
      .tapError(err => Console.printLineError(err.message).ignoreLogged)
      .mapError(_.status)
      .map(toGetResponse)
  }

  private def checkUserAuthorization(authorization: Option[Authorization]): IO[Status, String] = {
    authorization match {
      case Some(auth) => ZIO.succeed(auth.user)
      case None       => ZIO.fail(Status.UNAUTHENTICATED)
    }
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
