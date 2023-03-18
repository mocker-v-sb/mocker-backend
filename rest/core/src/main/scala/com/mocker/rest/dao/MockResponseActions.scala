package com.mocker.rest.dao

import com.mocker.rest.model.MockResponse
import slick.dbio.DBIO

trait MockResponseActions {
  def get(mockId: Long, responseId: Long): DBIO[Option[MockResponse]]
  def getAll(mockId: Long): DBIO[Seq[MockResponse]]
  def upsert(mockResponse: MockResponse): DBIO[Unit]

  def delete(mockId: Long, responseId: Long): DBIO[Unit]

  def deleteAll(mockId: Long): DBIO[Unit]
}
