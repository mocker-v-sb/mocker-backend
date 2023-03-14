package com.mocker.rest.errors

case class MockExistsException(servicePath: String, mockPath: String)
    extends IllegalArgumentException(s"Mock for path $mockPath already exists in service with path $servicePath")
