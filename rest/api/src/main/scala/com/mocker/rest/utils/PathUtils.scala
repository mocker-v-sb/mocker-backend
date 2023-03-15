package com.mocker.rest.utils

import com.mocker.rest.model.{Mock, MockResponse}

object PathUtils {

  def buildFullPath(mock: Mock, mockResponse: MockResponse): String = {
    val urlWithPathParams = mockResponse.pathParams.foldLeft(mock.path) {
      case (path, param) => path.replace(s"{${param.name}}", s"${param.value}")
    }
    mockResponse.queryParams.toList match {
      case Nil         => urlWithPathParams
      case head :: Nil => urlWithPathParams + s"?${head.name}=${head.value}"
      case head :: tail =>
        val n = urlWithPathParams + s"?${head.name}=${head.value}"
        tail.foldLeft(n) {
          case (path, param) => path + s"&${param.name}=${param.value}"
        }
    }
  }

}
