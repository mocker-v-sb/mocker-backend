package com.mocker.rest.utils

object Implicits {

  implicit class MapAny[T](private val x: T) extends AnyVal {

    def mapIf(condition: Boolean, f: => T => T): T = {
      if (condition)
        f(x)
      else x
    }
  }
}
