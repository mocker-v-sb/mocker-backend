package com.mocker.models.rest.requests.service

import com.google.protobuf.timestamp.Timestamp
import com.google.protobuf.util.Timestamps
import com.mocker.common.paging.{Page => ProtoPage}
import com.mocker.models.rest.common.{Method, ResponseSource, ResponseTimeSort}
import com.mocker.rest.rest_service.GetServiceResponseHistory.{Request => ProtoGetServiceHistoryRequest}

case class GetServiceResponseHistoryRequest(
    serviceId: Long,
    pageNum: Option[Int],
    pageSize: Option[Int],
    fromMillis: Option[Long],
    toMillis: Option[Long],
    searchUrl: Option[String],
    statusCodes: Seq[Int],
    responseSources: Seq[ResponseSource],
    methods: Seq[Method],
    timeSort: ResponseTimeSort
) {

  def toMessage: ProtoGetServiceHistoryRequest = {
    ProtoGetServiceHistoryRequest(
      page = Some(ProtoPage(num = pageNum.getOrElse(0), size = pageSize.getOrElse(10))),
      id = serviceId,
      fromMillis.map(Timestamps.fromMillis).map(t => Timestamp(t.getSeconds, t.getNanos)),
      toMillis.map(Timestamps.fromMillis).map(t => Timestamp(t.getSeconds, t.getNanos)),
      searchUrl = searchUrl,
      statusCodes = statusCodes,
      responseSources = responseSources.map(_.proto),
      requestMethods = methods.map(_.proto),
      responseTimeSort = timeSort.proto
    )
  }
}
