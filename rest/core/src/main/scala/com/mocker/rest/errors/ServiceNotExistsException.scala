package com.mocker.rest.errors

case class ServiceNotExistsException(path: String)
  extends IllegalArgumentException(s"Service with path $path does not exists")
