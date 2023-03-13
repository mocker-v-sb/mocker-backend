package com.mocker.common.utils

import scala.io.{Codec, Source}
import scala.util.Try

object SqlScript {

  val Delimiter = ";"

  def statementsFromFile(path: String): Option[Seq[String]] = {
    Try {
      val content = Source.fromResource(path)(Codec.UTF8).mkString
      content.split(Delimiter).map(_.trim).filter(_.nonEmpty).toSeq
    }.toOption
  }
}
