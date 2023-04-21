package com.mocker.rest.utils

import com.mocker.rest.request.KVPair
import zio.http.model.Headers
import zio.http.model.Headers.Header

object HeadersUtils {

  implicit class HeadersImplicits(private val headers: Headers) extends AnyVal {

    def toKVPairs: Seq[KVPair] = {
      headers.toList.map { case Header(key, value) => KVPair(key.toString, value.toString) }
    }
  }

}
