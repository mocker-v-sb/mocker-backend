package com.mocker.rest.utils

import com.mocker.rest.model.{Mock, MockQuery, MockResponse}
import com.mocker.rest.utils.PathUtils._
import com.mocker.rest.utils.Orderings._

object MockMatchers {

  implicit class MockFieldsMatcher(private val mock: Mock) extends AnyVal {

    def matches(query: MockQuery): Boolean = {
      mock.method == query.method &&
      mock.requestHeaders.sorted == query.headers.map(_.name).sorted &&
      mock.queryParams.sorted == query.queryParams.map(_.name).sorted &&
      query.requestPath.matchesPattern(mock.path)
    }
  }

  implicit class MockResponseFieldsMatcher(private val mockResponse: MockResponse) extends AnyVal {

    def matches(query: MockQuery): Boolean = {
      mockResponse.queryParams.sorted == query.queryParams.sorted &&
      mockResponse.requestHeaders == query.headers.sorted
    }
  }

}