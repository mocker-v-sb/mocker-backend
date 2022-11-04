package com.mocker.gateway

case class ServerAddress(port: Int, domain: String) {
  override def toString: String = s"$domain:$port"
}
