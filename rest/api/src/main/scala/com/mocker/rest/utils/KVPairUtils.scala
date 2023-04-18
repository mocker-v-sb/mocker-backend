package com.mocker.rest.utils

import com.mocker.rest.request.KVPair
import zio.http.QueryParams
import zio.http.model.Headers

object KVPairUtils {

  implicit class KVPairImplicits(private val kvPair: Seq[KVPair]) extends AnyVal {

    def toHttpHeaders: Headers = {
      ???
    }

    def toQueryParams: QueryParams = {
      ???
    }
  }

}
