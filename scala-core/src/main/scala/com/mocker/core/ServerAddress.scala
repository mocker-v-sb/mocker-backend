package com.mocker.core

case class ServerAddress(port: Int, domain: String) {
  override def toString: String = s"$domain:$port"
}
