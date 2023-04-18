package com.mocker.models.utils

import zio.http.URL
import zio.http.model.Scheme

object UrlBuilder {

  def asString(url: URL): String = {
    val scheme = url.scheme.getOrElse(Scheme.HTTP).encode
    val hostWithPort = url.hostWithOptionalPort.getOrElse("1.1.1.1:1")
    val path = url.path.encode
    val queryParams = url.queryParams.encode
    s"$scheme://$hostWithPort/$path${if (queryParams.nonEmpty) s"?$queryParams"}"
  }
}
