package com.mocker.rest.utils

import com.mocker.rest.errors.RestMockerException
import zio.{IO, Task}

object RestMockerUtils {

  implicit class ZIODbTaskRunner[R](private val task: Task[R]) extends AnyVal {

    def run: IO[RestMockerException, R] = {
      task.mapError(RestMockerException.internal)
    }
  }
}
