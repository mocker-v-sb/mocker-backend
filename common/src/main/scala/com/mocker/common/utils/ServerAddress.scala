package com.mocker.common.utils

case class ServerAddress(host: String, port: Int) {
  override def toString: String = s"$host:$port"
}
