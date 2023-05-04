package com.mocker.rest.dao

import com.mocker.rest.model.MockHistoryItem
import slick.dbio.DBIO

import java.sql.Timestamp

trait MockHistoryActions {

  def get(serviceId: Long): DBIO[Seq[MockHistoryItem]]

  def search(
      serviceId: Long,
      searchUrl: Option[String],
      from: Option[Timestamp],
      to: Option[Timestamp],
      limit: Int,
      shift: Int
  ): DBIO[Seq[MockHistoryItem]]

  def count(
      serviceId: Long,
      searchUrl: Option[String],
      from: Option[Timestamp],
      to: Option[Timestamp]
  ): DBIO[Int]

  def insert(item: MockHistoryItem): DBIO[Unit]
}
