package com.mocker.clients

import com.mocker.models.rest.requests.CreateServiceRequest
import com.mocker.rest.rest_service.CreateService.{Response => ProtoCreateServiceResponse}
import com.mocker.rest.rest_service.ZioRestService.RestMockerClient
import io.grpc.Status
import zio.ZIO

object RestMockerClientService {

  def createService(
      request: CreateServiceRequest
  ): ZIO[RestMockerClient.Service, Status, ProtoCreateServiceResponse] = {
    RestMockerClient.createService(request.toMessage)
  }

}
