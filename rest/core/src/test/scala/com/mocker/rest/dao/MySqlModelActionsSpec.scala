package com.mocker.rest.dao

import com.mocker.rest.dao.mysql.{MySqlModelActions, MySqlServiceActions}
import com.mocker.rest.gen.{ModelGen, ServiceGen}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class MySqlModelActionsSpec
    extends AnyWordSpecLike
    with Matchers
    with ServiceGen
    with ModelGen
    with RestMockerTestBase {

  "ModelActions" should {

    val serviceActions = MySqlServiceActions()
    val modelActions = MySqlModelActions()
    lazy val database = db.get

    "insert non-existing model" in withContainers { _ =>
      // Create service to connect models with it
      val service = sample(serviceGen)
      database.run(serviceActions.upsert(service)).futureValue

      // Create model for service
      val model = sample(modelGen).copy(serviceId = service.id)
      database.run(modelActions.upsert(model)).futureValue

      database.run(modelActions.getAll(service.id)).futureValue.toList shouldBe Seq(model)
    }

    "fail to insert model for non-existing service" in withContainers { _ =>
      intercept[Exception] {
        // Create model for no service
        val model = sample(modelGen)
        Await.result(database.run(modelActions.upsert(model)), 1.minute)
      }
    }

    "update existing models (by pk)" in withContainers { _ =>
      // Create service to connect models with it
      val service = sample(serviceGen)
      database.run(serviceActions.upsert(service)).futureValue

      // Create model for service
      val model = sample(modelGen).copy(serviceId = service.id)
      database.run(modelActions.upsert(model)).futureValue

      database.run(modelActions.getAll(service.id)).futureValue.toList shouldBe Seq(model)

      // Update model name in the row
      val updatedModel = model.copy(name = model.name + "-1")
      database.run(modelActions.upsert(updatedModel)).futureValue

      database.run(modelActions.getAll(service.id)).futureValue.toList shouldBe Seq(updatedModel)
    }
  }
}
