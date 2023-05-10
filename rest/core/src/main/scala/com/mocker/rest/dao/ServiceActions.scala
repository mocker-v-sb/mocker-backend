package com.mocker.rest.dao

import com.mocker.rest.model.{Service, ServiceStats}
import slick.dbio.DBIO

trait ServiceActions {
  def get(serviceId: Long): DBIO[Option[Service]]
  def get(path: String): DBIO[Option[Service]]
  def getAll: DBIO[Seq[Service]]
  def getWithStats(user: String): DBIO[Seq[ServiceStats]]
  def search(user: String, query: String): DBIO[Seq[ServiceStats]]
  def upsert(service: Service): DBIO[Unit]
  def delete(serviceId: Long): DBIO[Unit]

  def deleteExpired(): DBIO[Unit]
}
