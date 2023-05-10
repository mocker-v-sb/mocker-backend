package com.mocker.rest.scheduler

import com.mocker.rest.utils.ZIOSlick._
import com.mocker.rest.dao.mysql.MySqlServiceActions
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.utils.RestMockerUtils._
import slick.interop.zio.DatabaseProvider
import zio.{ZIO, ZLayer}

import scala.concurrent.ExecutionContext

object RestExpiredServiceCleanerTask {

  def dropExpiredServices()(implicit ec: ExecutionContext): ZIO[DatabaseProvider, RestMockerException, Unit] = {
    for {
      dbProvider <- ZIO.service[DatabaseProvider]
      dbLayer = ZLayer.succeed(dbProvider)
      _ <- zio.Console.printLine("Running task to clean services..").mapError(RestMockerException.internal)
      _ <- MySqlServiceActions().deleteExpired().asZIO(dbLayer).run
    } yield ()
  }
}
