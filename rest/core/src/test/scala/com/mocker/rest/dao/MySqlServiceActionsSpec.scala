package com.mocker.rest.dao

import com.mocker.rest.dao.mysql.MySqlServiceActions
import com.mocker.rest.gen.ServiceGen
import org.scalatest.Ignore
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.ExecutionContext.Implicits.global

@Ignore
class MySqlServiceActionsSpec extends AnyWordSpecLike with Matchers with ServiceGen with RestMockerTestBase {

  "ServiceActions" should {

    val serviceActions = MySqlServiceActions()
    lazy val database = db.get

    "insert non-existing service values" in withContainers { _ =>
      val service = sample(serviceGen)
      database.run(serviceActions.update(service)).futureValue
      database.run(serviceActions.getAll).futureValue.toList shouldBe Seq(service)
    }

    "update existing service values with new fields (by id)" in withContainers { _ =>
      // Create and insert service, check it
      val service = sample(serviceGen)
      database.run(serviceActions.update(service)).futureValue
      database.run(serviceActions.getAll).futureValue.toList shouldBe Seq(service)

      // Create new name for service, update row
      val updatedService = service.copy(name = service.name + "-1")
      database.run(serviceActions.update(updatedService)).futureValue

      database.run(serviceActions.getAll).futureValue.toList shouldBe Seq(updatedService)
    }

    "delete service by id" in withContainers { _ =>
      // Create and insert service, check it
      val service = sample(serviceGen)
      database.run(serviceActions.update(service)).futureValue
      database.run(serviceActions.getAll).futureValue.toList shouldBe Seq(service)

      database.run(serviceActions.delete(service.id)).futureValue
      database.run(serviceActions.getAll).futureValue.toList shouldBe Seq.empty
    }
  }
}
