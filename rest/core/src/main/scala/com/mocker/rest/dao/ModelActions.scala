package com.mocker.rest.dao

import com.mocker.rest.model.Model
import slick.dbio.DBIO

trait ModelActions {

  def get(modelId: Long): DBIO[Option[Model]]
  def get(serviceId: Long, modelId: Long): DBIO[Option[Model]]
  def getAll(serviceId: Long): DBIO[Seq[Model]]
  def delete(serviceId: Long, modelId: Long): DBIO[Unit]
  def deleteAll(serviceId: Long): DBIO[Unit]
  def upsert(model: Model): DBIO[Unit]
}
