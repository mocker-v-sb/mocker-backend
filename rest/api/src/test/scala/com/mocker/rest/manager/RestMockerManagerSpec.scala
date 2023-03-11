package com.mocker.rest.manager

import com.mocker.rest.dao.mysql.MySqlServiceActions
import com.mocker.rest.gen.ServiceGen
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import slick.interop.zio.DatabaseProvider

import scala.concurrent.ExecutionContext.Implicits.global

class RestMockerManagerSpec extends AnyWordSpecLike with Matchers with MockFactory with ServiceGen {

  lazy val dbProvider = mock[DatabaseProvider]
  lazy val serviceActions = new MySqlServiceActions()

  "RestMockerManager" should {

    "be able to create service" in {
      val manager = RestMockerManager(dbProvider, serviceActions)
      val service = Iterator.continually(serviceGen.sample).take(1).flatten.toSeq.head

      noException should be thrownBy manager.createService(service)
    }
  }

}
