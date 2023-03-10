package com.mocker.common.gen

import org.scalacheck.Gen

trait BasicGenerators {

  def alphaNumericString(min: Int, max: Int): Gen[String] =
    for {
      length <- Gen.choose(min, max)
      chars <- Gen.listOfN(length, Gen.alphaNumChar)
    } yield chars.mkString
}
