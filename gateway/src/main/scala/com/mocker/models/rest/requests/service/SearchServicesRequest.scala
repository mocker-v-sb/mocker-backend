package com.mocker.models.rest.requests.service

import com.mocker.rest.rest_service.SearchServices.{Request => ProtoSearchServicesRequest}

case class SearchServicesRequest(query: String) {

  def toMessage: ProtoSearchServicesRequest = {
    ProtoSearchServicesRequest(
      query = query
    )
  }
}
