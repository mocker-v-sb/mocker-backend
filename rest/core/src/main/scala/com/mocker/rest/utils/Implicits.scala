package com.mocker.rest.utils

import com.mocker.rest.model.Service
import zio.schema.{DeriveSchema, Schema}

object Implicits {

  object RedisImplicits {

    implicit val serviceSchema: Schema[Service] = DeriveSchema.gen[Service]
  }

  implicit class MapAny[T](private val x: T) extends AnyVal {

    def mapIf(condition: Boolean, f: => T => T): T = {
      if (condition)
        f(x)
      else x
    }
  }
}
