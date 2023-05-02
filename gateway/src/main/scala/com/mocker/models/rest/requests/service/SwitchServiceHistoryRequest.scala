package com.mocker.models.rest.requests.service

import com.mocker.rest.rest_service.SwitchServiceHistory.{Request => ProtoSwitchServiceHistory}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class SwitchServiceHistoryRequest(servicePath: String = "", isHistoryEnabled: Boolean) {

  def toMessage: ProtoSwitchServiceHistory = {
    ProtoSwitchServiceHistory(path = servicePath, isHistoryEnabled = isHistoryEnabled)
  }
}

object SwitchServiceHistoryRequest {
  implicit val encoder = DeriveJsonEncoder.gen[SwitchServiceHistoryRequest]
  implicit val decoder = DeriveJsonDecoder.gen[SwitchServiceHistoryRequest]
}
