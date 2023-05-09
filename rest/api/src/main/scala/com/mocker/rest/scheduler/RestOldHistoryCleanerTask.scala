package com.mocker.rest.scheduler

import com.mocker.rest.dao.mysql.{MySqlMockHistoryActions, MySqlServiceActions}
import com.mocker.rest.errors.RestMockerException
import com.mocker.rest.utils.RestMockerUtils._
import com.mocker.rest.utils.ZIOSlick._
import slick.interop.zio.DatabaseProvider
import zio.{ZIO, ZLayer}

import scala.concurrent.ExecutionContext

object RestOldHistoryCleanerTask {

  def dropOldHistoryRecords()(implicit ec: ExecutionContext): ZIO[DatabaseProvider, RestMockerException, Unit] = {
    for {
      dbProvider <- ZIO.service[DatabaseProvider]
      dbLayer = ZLayer.succeed(dbProvider)
      _ <- MySqlMockHistoryActions().deleteOldRecords().asZIO(dbLayer).run
    } yield ()
  }
}
