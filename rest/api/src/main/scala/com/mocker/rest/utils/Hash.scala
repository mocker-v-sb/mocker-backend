package com.mocker.rest.utils

import com.mocker.rest.model.{Mock, MockResponse, Service}
import com.mocker.rest.request.{KVPair, Method}
import com.mocker.rest.utils.Orderings.kvPairOrdering

import scala.util.hashing.MurmurHash3

object Hash {

  def getHash(
      servicePath: String,
      mockPath: String,
      method: Method,
      queryParams: Set[KVPair],
      headers: Set[KVPair]
  ): Int = {
    MurmurHash3.productHash(
      (
        servicePath,
        if (mockPath.startsWith("/")) mockPath.drop(1) else mockPath,
        method,
        queryParams.toSeq.sorted,
        headers.toSeq.sorted
      )
    )
  }

  def getHash(servicePath: String, mock: Mock, mockResponse: MockResponse): Int = {
    getHash(
      servicePath,
      PathUtils.buildWithPathParams(mock, mockResponse),
      mock.method,
      mockResponse.queryParams,
      mockResponse.requestHeaders
    )
  }

}
