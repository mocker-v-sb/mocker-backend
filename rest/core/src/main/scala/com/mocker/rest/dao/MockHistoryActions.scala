package com.mocker.rest.dao

import com.mocker.rest.model.MockHistoryItem
import slick.dbio.DBIO

trait MockHistoryActions {

  def get(serviceId: Long): DBIO[Seq[MockHistoryItem]]

  def insert(item: MockHistoryItem): DBIO[Unit]
}
