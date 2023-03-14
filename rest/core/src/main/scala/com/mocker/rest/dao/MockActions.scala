package com.mocker.rest.dao

import com.mocker.rest.model.Mock
import slick.dbio.DBIO

trait MockActions {
  def get(serviceId: Long, path: String): DBIO[Option[Mock]]

  def getAll(serviceId: Long): DBIO[Seq[Mock]]

  def upsert(mock: Mock): DBIO[Unit]
}
