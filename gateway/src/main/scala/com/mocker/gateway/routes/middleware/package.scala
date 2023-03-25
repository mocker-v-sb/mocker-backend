package com.mocker.gateway.routes

import zhttp.http._
import zhttp.http.middleware.Cors.CorsConfig

package object middleware {

  val corsConfig: CorsConfig = CorsConfig(
    anyOrigin = true,
    anyMethod = true,
    allowedOrigins = _ => true,
    allowedMethods = Some(Set(Method.GET, Method.POST, Method.PUT, Method.PATCH, Method.DELETE))
  )
}
