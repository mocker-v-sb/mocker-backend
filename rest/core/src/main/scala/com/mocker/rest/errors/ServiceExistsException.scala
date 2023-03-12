package com.mocker.rest.errors

case class ServiceExistsException(path: String)
    extends IllegalArgumentException(s"Service with path $path already exists")
