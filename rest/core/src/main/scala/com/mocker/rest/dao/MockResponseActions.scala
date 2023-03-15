package com.mocker.rest.dao

import com.mocker.rest.model.MockResponse
import slick.dbio.DBIO

trait MockResponseActions {
  def upsert(mockResponse: MockResponse): DBIO[Unit]
}
