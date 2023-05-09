package com.mocker.rest.dao

import com.mocker.rest.model.{Service, ServiceStats}
import slick.dbio.DBIO

trait ServiceActions {
  def get(serviceId: Long): DBIO[Option[Service]]
  def get(path: String): DBIO[Option[Service]]
  def getAll: DBIO[Seq[Service]]
  def getWithStats: DBIO[Seq[ServiceStats]]
  def search(query: String): DBIO[Seq[ServiceStats]]
  def insert(service: Service): DBIO[Service]
  def update(service: Service): DBIO[Unit]
  def delete(serviceId: Long): DBIO[Unit]

  def deleteExpired(): DBIO[Unit]
}
