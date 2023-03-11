package com.mocker.rest.gen

import com.mocker.common.gen.BasicGenerators
import com.mocker.rest.model.Service
import org.scalacheck.Gen

import java.sql.Timestamp
import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import scala.concurrent.duration._

trait ServiceGen extends BasicGenerators {

  private val PATTERN_FORMAT = "yyyy-MM-dd HH:mm:ss"

  private val formatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern(PATTERN_FORMAT)
    .withZone(ZoneId.systemDefault())

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
      createTime = Timestamp.valueOf(formatter.format(Instant.now())),
      updateTime = Timestamp.valueOf(formatter.format(Instant.now())),
      expirationTime = Some(Timestamp.valueOf(formatter.format(Instant.now().plusMillis(ttl.toMillis))))
    )
}
