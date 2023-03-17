package com.mocker.rest.errors

import io.grpc.Status

case class RestMockerException(message: String, status: Status) extends RuntimeException(message)

object RestMockerException {

  def internal(throwable: Throwable): RestMockerException =
    RestMockerException(throwable.getMessage, Status.INTERNAL)

  def serviceAlreadyExists(path: String): RestMockerException =
    RestMockerException(s"Service with path $path already exists", Status.ALREADY_EXISTS)

  def serviceNotExists(path: String): RestMockerException =
    RestMockerException(s"Service with path $path does not exists", Status.NOT_FOUND)

  def mockAlreadyExists(servicePath: String, mockPath: String): RestMockerException =
    RestMockerException(
      s"Mock for path $mockPath already exists in service with path $servicePath",
      Status.ALREADY_EXISTS
    )

  def mockNotExists(mockId: Long): RestMockerException =
    RestMockerException(s"Mock with id $mockId does not exists", Status.NOT_FOUND)

  def modelNotExists(servicePath: String, modelId: Long): RestMockerException =
    RestMockerException(s"Model with id $modelId does not exists for service $servicePath", Status.NOT_FOUND)

  def mockNotExists(servicePath: String, mockId: Long): RestMockerException =
    RestMockerException(s"Mock with id $mockId does not exists for service $servicePath", Status.NOT_FOUND)

  def responseNotExists(mockId: Long, responseId: Long): RestMockerException =
    RestMockerException(s"Response with id $responseId does not exists for mock $mockId", Status.NOT_FOUND)

  def invalidMockResponse(mockPath: String, name: String): RestMockerException =
    RestMockerException(
      s"Mock response $name contains invalid arguments for mock at path $mockPath",
      Status.INVALID_ARGUMENT
    )
}
