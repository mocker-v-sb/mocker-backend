package com.mocker.rest.errors

import com.mocker.rest.model.Mock
import io.grpc.Status

case class RestMockerException(message: String, status: Status) extends RuntimeException(message)

object RestMockerException {

  def internal(throwable: Throwable): RestMockerException =
    RestMockerException(throwable.getMessage, Status.INTERNAL)

  def accessDenied(user: String, servicePath: String): RestMockerException =
    RestMockerException(s"Access denied for user $user for service $servicePath", Status.PERMISSION_DENIED)

  def cantGetProxiedResponse(throwable: Throwable): RestMockerException =
    RestMockerException(throwable.getMessage, Status.INTERNAL)

  def serviceAlreadyExists(path: String): RestMockerException =
    RestMockerException(s"Service with path $path already exists", Status.ALREADY_EXISTS)

  def serviceNotExists(path: String): RestMockerException =
    RestMockerException(s"Service with path $path does not exists", Status.NOT_FOUND)

  def serviceNotExists(id: Long): RestMockerException =
    RestMockerException(s"Service with id $id does not exists", Status.NOT_FOUND)

  def incorrectServicePath(servicePath: String): RestMockerException =
    RestMockerException(s"Path $servicePath for service is invalid", Status.INVALID_ARGUMENT)

  def proxyUrlMissing(servicePath: String): RestMockerException =
    RestMockerException(s"Service $servicePath has proxy enabled, but no url is defined", Status.INVALID_ARGUMENT)

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

  def wrongSample(sample: String): RestMockerException =
    RestMockerException(s"""Sample "$sample" has invalid format""", Status.INVALID_ARGUMENT)

  def responseNotExists(mockId: Long, responseId: Long): RestMockerException =
    RestMockerException(s"Response with id $responseId does not exists for mock $mockId", Status.NOT_FOUND)

  def modelInUse(servicePath: String, mocks: Seq[Mock]): RestMockerException =
    RestMockerException(
      s"Service at path $servicePath uses this model in mocks ${mocks.map(_.path).mkString(",")}",
      Status.INVALID_ARGUMENT
    )

  def invalidMockResponse(mockPath: String, name: String): RestMockerException =
    RestMockerException(
      s"Mock response $name contains invalid arguments for mock at path $mockPath",
      Status.INVALID_ARGUMENT
    )

  def suitableMockNotFound: RestMockerException =
    RestMockerException("Could not find suitable mock", Status.NOT_FOUND)
}
