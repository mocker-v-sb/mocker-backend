package com.mocker.rest.utils

import com.mocker.rest.request.KVPair
import zio.Chunk
import zio.http.QueryParams
import zio.http.model.{Header, Headers}

object KVPairUtils {

  implicit class KVPairImplicits(private val kvPair: Seq[KVPair]) extends AnyVal {

    def toHttpHeaders: Headers = {
      Headers(
        kvPair.map { case KVPair(key, value) => Header(key, value) }
      )
    }

    def toQueryParams: QueryParams = {
      QueryParams(
        kvPair
          .groupBy(_.name)
          .map {
            case (name, pairs) =>
              (name, Chunk.fromIterable(pairs.map(_.value)))
          }
      )
    }
  }

}
