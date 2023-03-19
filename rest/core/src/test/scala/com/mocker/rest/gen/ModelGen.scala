package com.mocker.rest.gen

import com.mocker.common.gen.BasicGenerators
import com.mocker.rest.model.Model
import org.scalacheck.Gen

import java.sql.Timestamp
import java.time.Instant

trait ModelGen extends BasicGenerators {

  def modelGen: Gen[Model] =
    for {
      id <- Gen.posNum[Long]
      name <- alphaNumericString(2, 10)
      description <- alphaNumericString(20, 50)
    } yield Model(
      id = id,
      name = name,
      description = Some(description),
      schema = "{}"
    )
}
