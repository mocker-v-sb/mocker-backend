package com.mocker.rest.avro

final case class SanitizedFieldName(validFieldName: String, explanation: Option[String])

/**
 * Credits to https://github.com/irajhedayati/savro
 */
object SanitizedFieldName {

  def apply(fieldName: String): SanitizedFieldName = {
    val sanitizedFieldName = fieldName
      .replaceAll("[^A-Za-z0-9_]+", "_")
      .replaceAll("_+$", "")
      .replaceAll("^_+", "")
    if (sanitizedFieldName.equals(fieldName)) SanitizedFieldName(fieldName, None)
    else
      SanitizedFieldName(
        sanitizedFieldName,
        Option(
          s"The original field name was '$fieldName' but some characters is not accepted in " +
          "the field name of Avro record"
        )
      )
  }
}
