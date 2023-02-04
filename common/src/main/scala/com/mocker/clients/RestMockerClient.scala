package com.mocker.clients

import com.mocker.rest.rest_model.{CreateMockRequest, CreateMockResponse}

import scala.concurrent.Future

trait RestMockerClient {

  def createMock(request: CreateMockRequest): Future[CreateMockResponse]

}
