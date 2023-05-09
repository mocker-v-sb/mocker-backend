package com.mocker.rest.utils

import com.mocker.rest.model.{Mock, MockQuery, MockResponse}
import com.mocker.rest.utils.PathUtils._
import com.mocker.rest.utils.Orderings._

object MockMatchers {

  implicit class MockFieldsMatcher(private val mock: Mock) extends AnyVal {

    def matches(query: MockQuery): Boolean = {
      mock.method == query.method &&
      mock.requestHeaders.forall(n => query.headers.map(_.name).contains(n)) && // query can contain extra headers
      mock.queryParams.sorted == query.queryParams.map(_.name).toSeq.sorted &&
      query.requestPath.matchesPattern(mock.path)
    }
  }

  implicit class MockResponseFieldsMatcher(private val mockResponse: MockResponse) extends AnyVal {

    def matches(query: MockQuery): Boolean = {
      mockResponse.queryParams.toSeq.sorted == query.queryParams.toSeq.sorted &&
      mockResponse.requestHeaders.forall(n => query.headers.contains(n)) // query can contain extra headers
    }
  }

}
