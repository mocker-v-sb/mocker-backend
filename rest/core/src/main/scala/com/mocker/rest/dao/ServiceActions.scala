package com.mocker.rest.dao

import com.mocker.rest.model.Service
import slick.dbio.DBIO

trait ServiceActions {
  def get(serviceId: Long): DBIO[Option[Service]]
  def get(path: String): DBIO[Option[Service]]
  def getAll: DBIO[Seq[Service]]
  def upsert(service: Service): DBIO[Unit]
  def delete(serviceId: Long): DBIO[Unit]
}
