package com.mocker.common.utils

case class ServerAddress(address: String, port: Int) {
  override def toString: String = s"$address:$port"
}
