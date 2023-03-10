package com.mocker.rest.gen

import com.mocker.common.gen.BasicGenerators
import com.mocker.rest.model.Service
import org.scalacheck.Gen

import java.time.Instant
import scala.concurrent.duration._

trait ServiceGen extends BasicGenerators {

  def serviceGen: Gen[Service] =
    for {
      id <- Gen.posNum[Long]
      name <- alphaNumericString(2, 10)
      path <- alphaNumericString(3, 10)
      url <- alphaNumericString(8, 12)
      description <- alphaNumericString(20, 50)
      ttl <- Gen.choose(1.hour, 7.days)
    } yield Service(
      id = id,
      name = name,
      path = path,
      url = Some(url),
      description = Some(description),
      createTime = Instant.now(),
      updateTime = Instant.now(),
      expirationTime = Some(Instant.now().plusMillis(ttl.toMillis))
    )
}
