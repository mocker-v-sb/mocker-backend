package com.mocker.rest.dao

import com.mocker.rest.mock_history.ResponseSourceNamespace.ResponseSource
import com.mocker.rest.mock_history.ResponseTimeSort
import com.mocker.rest.model.MockHistoryItem
import com.mocker.rest.request.Method
import slick.dbio.DBIO

import java.time.Instant

trait MockHistoryActions {

  def get(serviceId: Long): DBIO[Seq[MockHistoryItem]]

  def search(
      serviceId: Long,
      searchUrl: Option[String],
      from: Option[Instant],
      to: Option[Instant],
      statusCodes: Set[Int],
      responseSources: Set[ResponseSource],
      methods: Set[Method],
      sort: ResponseTimeSort,
      limit: Int,
      shift: Int
  ): DBIO[Seq[MockHistoryItem]]

  def count(
      serviceId: Long,
      searchUrl: Option[String],
      from: Option[Instant],
      to: Option[Instant],
      statusCodes: Set[Int],
      responseSources: Set[ResponseSource],
      methods: Set[Method]
  ): DBIO[Int]

  def insert(item: MockHistoryItem): DBIO[Unit]
}
