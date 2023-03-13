package com.mocker.common.gen

import org.scalacheck.Gen

import java.time.ZoneId
import java.time.format.DateTimeFormatter

trait BasicGenerators {

  private val PATTERN_FORMAT = "yyyy-MM-dd HH:mm:ss"

  protected val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern(PATTERN_FORMAT)
    .withZone(ZoneId.systemDefault())

  protected def sample[T](gen: Gen[T]): T =
    Iterator.continually(gen.sample).take(1).flatten.toSeq.head

  def alphaNumericString(min: Int, max: Int): Gen[String] =
    for {
      length <- Gen.choose(min, max)
      chars <- Gen.listOfN(length, Gen.alphaNumChar)
    } yield chars.mkString
}
