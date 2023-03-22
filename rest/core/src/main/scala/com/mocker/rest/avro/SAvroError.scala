package com.mocker.rest.avro

import io.circe.Json

/**
  * Credits to https://github.com/irajhedayati/savro
  */
sealed trait SAvroError extends Throwable

sealed trait AvroSchemaError extends SAvroError {
  val input: Object
  val message: String
  override def toString: String =
    s"""Input: ${input.toString}
       |Message: $message""".stripMargin
}

object AvroSchemaError {

  final case class IllegalJsonInput(override val input: Json, override val message: String) extends AvroSchemaError

}
