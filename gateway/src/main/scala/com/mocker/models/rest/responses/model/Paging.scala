package com.mocker.models.rest.responses.model

import com.mocker.common.paging.{Paging => ProtoPaging}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class Paging(totalItems: Long, totalPages: Long, page: Option[Int], pageSize: Option[Int])

object Paging {
  implicit val encoder = DeriveJsonEncoder.gen[Paging]
  implicit val decoder = DeriveJsonDecoder.gen[Paging]

  def fromMessage(message: ProtoPaging): Paging = {
    Paging(
      totalItems = message.totalItems,
      totalPages = message.totalPages,
      page = message.page.map(_.num),
      pageSize = message.page.map(_.size)
    )
  }
}
