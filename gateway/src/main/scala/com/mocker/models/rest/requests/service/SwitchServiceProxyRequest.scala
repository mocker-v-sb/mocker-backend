package com.mocker.models.rest.requests.service

import com.mocker.rest.rest_service.SwitchServiceProxy.{Request => ProtoSwitchServiceProxyRequest}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class SwitchServiceProxyRequest(servicePath: String = "", isProxyEnabled: Boolean) {

  def toMessage: ProtoSwitchServiceProxyRequest = {
    ProtoSwitchServiceProxyRequest(path = servicePath, isProxyEnabled = isProxyEnabled)
  }
}

object SwitchServiceProxyRequest {
  implicit val encoder = DeriveJsonEncoder.gen[SwitchServiceProxyRequest]
  implicit val decoder = DeriveJsonDecoder.gen[SwitchServiceProxyRequest]
}
