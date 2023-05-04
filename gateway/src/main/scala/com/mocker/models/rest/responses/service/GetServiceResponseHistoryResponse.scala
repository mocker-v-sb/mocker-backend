package com.mocker.models.rest.responses.service

import com.mocker.models.rest.responses.model.{HistoryItem, Paging}
import com.mocker.rest.rest_service.GetServiceResponseHistory.{Response => ProtoGetServiceResponseHistoryResponse}
import zio.json.DeriveJsonEncoder

case class GetServiceResponseHistoryResponse(
    paging: Option[Paging],
    items: Seq[HistoryItem]
)

object GetServiceResponseHistoryResponse {
  implicit val encoder = DeriveJsonEncoder.gen[GetServiceResponseHistoryResponse]

  def fromMessage(message: ProtoGetServiceResponseHistoryResponse): GetServiceResponseHistoryResponse = {
    GetServiceResponseHistoryResponse(
      paging = message.paging.map(Paging.fromMessage),
      items = message.items.map(HistoryItem.fromMessage)
    )
  }
}
