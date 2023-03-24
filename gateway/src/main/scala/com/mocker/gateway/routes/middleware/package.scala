package com.mocker.gateway.routes

import zhttp.http.middleware.Cors.CorsConfig

package object middleware {
  val corsConfig: CorsConfig = CorsConfig(
    anyOrigin = true,
    anyMethod = true
  )
}
