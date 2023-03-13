package com.mocker.rest.dao

import com.dimafeng.testcontainers.MySQLContainer
import com.mocker.rest.dao.mysql.MySqlServiceActions
import com.mocker.rest.gen.ServiceGen
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpecLike
import org.testcontainers.utility.DockerImageName

import scala.concurrent.ExecutionContext.Implicits.global

class MySqlServiceActionsSpec
    extends AnyWordSpecLike
    with Matchers
    with ServiceGen
    with RestMockerTestBase {

  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(1, Seconds), interval = Span(500, Millis))

  override val containerDef: MySQLContainer.Def = MySQLContainer.Def(
    dockerImageName = DockerImageName.parse("mysql:8.0.31")
  )

  "ServiceActions" should {

    val serviceActions = MySqlServiceActions()
    lazy val database = db.get

    "insert non-existing values" in withContainers { _ =>
      val service = Iterator.continually(serviceGen.sample).take(1).flatten.toSeq.head
      database.run(serviceActions.upsert(service)).futureValue
      database.run(serviceActions.getAll).futureValue.toList shouldBe Seq(service)
    }

    "update existing values" in withContainers { _ =>
      val service = Iterator.continually(serviceGen.sample).take(1).flatten.toSeq.head
      database.run(serviceActions.upsert(service)).futureValue
      val updatedService = service.copy(name = service.name + "-1")
      database.run(serviceActions.upsert(updatedService)).futureValue

      database.run(serviceActions.getAll).futureValue.toList shouldBe Seq(updatedService)
    }

    "delete values" in withContainers { _ =>
      val service = Iterator.continually(serviceGen.sample).take(1).flatten.toSeq.head
      database.run(serviceActions.upsert(service)).futureValue
      database.run(serviceActions.delete(service.id)).futureValue

      database.run(serviceActions.getAll).futureValue.toList shouldBe Seq.empty
    }
  }
}
