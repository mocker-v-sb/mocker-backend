package com.mocker.rest.utils

import com.mocker.rest.avro.AvroSchema
import io.circe.parser._
import com.mocker.rest.errors.RestMockerException
import org.apache.avro.Schema
import org.apache.avro.util.RandomData

object AvroSchemaUtils {

  def fromSample(sample: String): String = {
    parse(sample)
      .flatMap(json => AvroSchema.inferRecord(json, "Schema", None))
      .getOrElse(throw RestMockerException.wrongSample(sample))
      .toString
  }

  def generateSample(schema: String): String = {
    val sample = new Schema.Parser().parse(schema)
    val data = new RandomData(sample, 1).iterator.next()
    String.valueOf(data)
  }

}
