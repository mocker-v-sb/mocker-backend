package com.mocker.rest.utils

import com.mocker.rest.request.Method
import zio.http.model.{Method => ZIOMethod}

object MethodUtils {

  implicit class MethodImplicits(private val method: Method) extends AnyVal {

    def toZIOMethod: ZIOMethod = {
      method match {
        case Method.GET    => ZIOMethod.GET
        case Method.POST   => ZIOMethod.POST
        case Method.PUT    => ZIOMethod.PUT
        case Method.DELETE => ZIOMethod.DELETE
        case Method.PATCH  => ZIOMethod.PATCH
      }
    }
  }

}
