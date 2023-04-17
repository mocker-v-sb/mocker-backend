package com.mocker.rest.utils

import slick.dbio.DBIO
import slick.interop.zio.DatabaseProvider
import slick.interop.zio.syntax._
import zio.{Task, ZIO}

object ZIOSlick {

  implicit class ZIOfromDBIO[R](private val dbio: DBIO[R]) extends AnyVal {

    def asZIO(layer: zio.ULayer[DatabaseProvider]): Task[R] = {
      ZIO.fromDBIO(dbio).provide(layer)
    }
  }

}
