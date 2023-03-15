package com.mocker.rest.dao

import com.mocker.rest.model.Model
import slick.dbio.DBIO

trait ModelActions {
  def get(serviceId: Long, modelId: Long): DBIO[Option[Model]]
  def getAll(serviceId: Long): DBIO[Seq[Model]]
  def upsert(model: Model): DBIO[Unit]
}
