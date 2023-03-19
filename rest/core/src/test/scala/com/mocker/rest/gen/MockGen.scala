package com.mocker.rest.gen

import com.mocker.common.gen.BasicGenerators
import com.mocker.rest.model.Mock
import com.mocker.rest.request.Method
import org.scalacheck.Gen

import java.sql.Timestamp
import java.time.Instant

trait MockGen extends BasicGenerators {

  def mockGen: Gen[Mock] =
    for {
      id <- Gen.posNum[Long]
      name <- alphaNumericString(2, 10)
      path <- alphaNumericString(3, 10)
      description <- alphaNumericString(20, 50)
      methodId <- Gen.chooseNum(0, 4)
    } yield Mock(
      id = id,
      name = name,
      description = Some(description),
      path = path,
      method = Method.fromValue(methodId),
      requestModelId = None,
      responseModelId = None,
      requestHeaders = Seq.empty,
      responseHeaders = Seq.empty,
      queryParams = Seq.empty,
      pathParams = Seq.empty
    )
}
