package com.mocker.models.rest.responses.model

import com.mocker.rest.mock_history.{HistoryItem => ProtoHistoryItem}
import com.mocker.models.rest.common.{KVPair, Method, ResponseSource}
import zio.json.DeriveJsonEncoder

case class HistoryItem(
    id: Long,
    method: Method,
    queryUrl: String,
    responseUrl: String,
    responseSource: ResponseSource,
    statusCode: Int,
    responseHeaders: Seq[KVPair],
    responseTime: Option[Long],
    response: String
)

object HistoryItem {

  implicit val encoder = DeriveJsonEncoder.gen[HistoryItem]

  def fromMessage(item: ProtoHistoryItem): HistoryItem = {
    HistoryItem(
      id = item.id,
      method = Method.forName(item.method.name),
      queryUrl = item.queryUrl,
      responseUrl = item.responseUrl,
      responseSource = ResponseSource.forName(item.responseSource.name),
      statusCode = item.statusCode,
      responseHeaders = item.responseHeaders.map(KVPair.fromProto),
      responseTime = item.responseTime.map(_.asJavaInstant.toEpochMilli),
      response = item.response
    )
  }
}
