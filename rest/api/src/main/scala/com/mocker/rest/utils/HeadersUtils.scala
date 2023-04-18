package com.mocker.rest.utils

import com.mocker.rest.request.KVPair
import zio.http.model.Headers

object HeadersUtils {

  implicit class HeadersImplicits(private val headers: Headers) extends AnyVal {

    def toKVPairs: Seq[KVPair] = {
      ???
    }
  }

}
