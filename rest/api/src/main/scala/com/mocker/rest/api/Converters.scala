package com.mocker.rest.api

import com.mocker.rest.model.{Model, Service}
import com.mocker.rest.rest_service.{CreateModelRequest, CreateServiceRequest}

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

}
