package com.mocker

import com.mocker.common.utils.ServerAddress

import scala.language.implicitConversions

package object common {
  implicit def serverAddress2String(serverAddress: ServerAddress): String = serverAddress.toString
}
