package com.mocker.rest.api

import com.mocker.rest.model.{Mock, Model, Service}
import com.mocker.rest.rest_service.{CreateMockRequest, CreateModelRequest, CreateServiceRequest}

import java.sql.Timestamp
import java.time.Instant

object Converters {

  def convertCreateServiceRequest(request: CreateServiceRequest): Service = {
    Service(
      name = request.name,
      path = request.path,
      url = request.url,
      description = request.description,
      creationTime = Timestamp.from(Instant.now()),
      updateTime = Timestamp.from(Instant.now()),
      expirationTime = request.expirationTime.map(t => Timestamp.from(t.asJavaInstant))
    )
  }

  def convertCreateModelRequest(request: CreateModelRequest): Model = {
    Model(
      name = request.name,
      description = request.description,
      creationTime = Timestamp.from(Instant.now())
    )
  }

  def convertCreateMockRequest(request: CreateMockRequest): Mock = {
    Mock(
      name = request.name,
      description = request.description,
      path = request.path,
      method = request.method,
      requestModelId = request.requestModelId,
      responseModelId = request.responseModelId,
      requestHeaders = request.requestHeaders,
      responseHeaders = request.responseHeaders,
      queryParams = request.queryParams,
      pathParams = request.pathParams,
      creationTime = Timestamp.from(Instant.now())
    )
  }

}
