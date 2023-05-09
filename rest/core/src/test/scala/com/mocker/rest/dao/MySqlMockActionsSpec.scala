package com.mocker.rest.dao

import com.mocker.rest.dao.mysql.{MySqlMockActions, MySqlModelActions, MySqlServiceActions}
import com.mocker.rest.gen.{MockGen, ModelGen, ServiceGen}
import org.scalatest.Ignore
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

@Ignore
class MySqlMockActionsSpec
    extends AnyWordSpecLike
    with Matchers
    with ServiceGen
    with ModelGen
    with MockGen
    with RestMockerTestBase {

  "MockActions" should {

    val serviceActions = MySqlServiceActions()
    val modelActions = MySqlModelActions()
    val mockActions = MySqlMockActions()
    lazy val database = db.get

    "insert non-existing mock" in withContainers { _ =>
      // Create service to connect mocks with it
      val service = sample(serviceGen)
      database.run(serviceActions.update(service)).futureValue

      // Create model for service
      val mock = sample(mockGen).copy(serviceId = service.id)
      database.run(mockActions.upsert(mock)).futureValue

      database.run(mockActions.getAll(service.id)).futureValue.toList shouldBe Seq(mock)
    }

    "insert mock with reference to models" in withContainers { _ =>
      // Create service to connect mocks with it
      val service = sample(serviceGen)
      database.run(serviceActions.update(service)).futureValue

      // Create model example
      val model = sample(modelGen).copy(serviceId = service.id)
      database.run(modelActions.upsert(model)).futureValue

      // Create model for service
      val mock =
        sample(mockGen).copy(serviceId = service.id, requestModelId = Some(model.id), responseModelId = Some(model.id))
      database.run(mockActions.upsert(mock)).futureValue

      database.run(mockActions.getAll(service.id)).futureValue.toList shouldBe Seq(mock)
    }

    "fail to insert mock for non-existing service" in withContainers { _ =>
      intercept[Exception] {
        // Create model for missing service
        val mock = sample(mockGen)
        Await.result(database.run(mockActions.upsert(mock)), 1.minute)
      }
    }

  }
}
