package com.mocker.rest.dao

import com.mocker.rest.model.Mock
import slick.dbio.DBIO

trait MockActions {
  def get(serviceId: Long, path: String): DBIO[Option[Mock]]
  def get(mockId: Long): DBIO[Option[Mock]]
  def get(serviceId: Long, mockId: Long): DBIO[Option[Mock]]

  def getAll(serviceId: Long): DBIO[Seq[Mock]]

  def upsert(mock: Mock): DBIO[Unit]

  def findByModel(serviceId: Long, modelId: Long): DBIO[Seq[Mock]]

  def delete(serviceId: Long, mockId: Long): DBIO[Unit]

  def deleteAll(serviceId: Long): DBIO[Unit]
}
