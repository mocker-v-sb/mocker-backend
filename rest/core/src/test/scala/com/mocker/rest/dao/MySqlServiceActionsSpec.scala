package com.mocker.rest.dao

import com.dimafeng.testcontainers.MySQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForEach
import com.mocker.rest.dao.mysql.MySqlServiceActions
import com.mocker.rest.gen.ServiceGen
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpecLike
import org.testcontainers.utility.DockerImageName
import slick.jdbc.JdbcBackend

import java.sql.DriverManager
import scala.concurrent.ExecutionContext.Implicits.global

class MySqlServiceActionsSpec
    extends AnyWordSpecLike
    with TestContainerForEach
    with Matchers
    with ScalaFutures
    with ServiceGen {

  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(1, Seconds), interval = Span(500, Millis))

  override val containerDef: MySQLContainer.Def = MySQLContainer.Def(
    dockerImageName = DockerImageName.parse("mysql:8.0.31")
  )

  override def startContainers(): MySQLContainer = {
    val mySqlContainer = super.startContainers()

    val connection = DriverManager.getConnection(
      mySqlContainer.container.getJdbcUrl,
      mySqlContainer.container.getUsername,
      mySqlContainer.container.getPassword
    )

    Class.forName(mySqlContainer.container.getDriverClassName)

    val createTableStatement =
      connection.prepareStatement("""
        |CREATE TABLE `service`
        |(
        |    `id`              BIGINT(16)   NOT NULL AUTO_INCREMENT,
        |    `name`            VARCHAR(128) NOT NULL,
        |    `path`            VARCHAR(128) NOT NULL,
        |    `url`             VARCHAR(128) NULL,
        |    `description`     VARCHAR(128) NULL,
        |    `creation_time`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
        |    `update_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
        |    `expiration_time` TIMESTAMP    NULL     DEFAULT NULL,
        |    PRIMARY KEY (`id`),
        |    UNIQUE INDEX `idx_name` (`name` ASC) VISIBLE,
        |    UNIQUE INDEX `idx_path` (`path` ASC) VISIBLE,
        |    UNIQUE INDEX `idx_url` (`url` ASC) VISIBLE
        |);
        |""".stripMargin)
    createTableStatement.execute()
    connection.close()
    mySqlContainer
  }

  "ServiceActions" should {

    val serviceActions = MySqlServiceActions()

    def initDatabase(containers: Containers) =
      JdbcBackend.Database.forURL(
        url = containers.container.getJdbcUrl,
        user = containers.container.getUsername,
        password = containers.container.getPassword
      )

    "insert non-existing values" in withContainers { mysqlContainer =>
      val db = initDatabase(mysqlContainer)

      val service = Iterator.continually(serviceGen.sample).take(1).flatten.toSeq.head
      db.run(serviceActions.upsert(service)).futureValue
      db.run(serviceActions.getAll).futureValue.toList shouldBe Seq(service)
    }

    "update existing values" in withContainers { mysqlContainer =>
      val db = initDatabase(mysqlContainer)

      val service = Iterator.continually(serviceGen.sample).take(1).flatten.toSeq.head
      db.run(serviceActions.upsert(service)).futureValue
      val updatedService = service.copy(name = service.name + "-1")
      db.run(serviceActions.upsert(updatedService)).futureValue

      db.run(serviceActions.getAll).futureValue.toList shouldBe Seq(updatedService)
    }

    "delete values" in withContainers { mysqlContainer =>
      val db = initDatabase(mysqlContainer)

      val service = Iterator.continually(serviceGen.sample).take(1).flatten.toSeq.head
      db.run(serviceActions.upsert(service)).futureValue
      db.run(serviceActions.delete(service.id)).futureValue

      db.run(serviceActions.getAll).futureValue.toList shouldBe Seq.empty
    }
  }
}
