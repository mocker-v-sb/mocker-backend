package com.mocker.rest.dao

import com.mocker.rest.model.Mock
import slick.dbio.DBIO

trait MockActions {
  def get(path: String): DBIO[Option[Mock]]

  def upsert(mock: Mock): DBIO[Unit]
}
