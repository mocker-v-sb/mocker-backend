package com.mocker.rest.dao

import com.dimafeng.testcontainers.MySQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForEach
import com.mocker.rest.dao.mysql.MySqlServiceActions
import com.mocker.rest.gen.ServiceGen
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
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
        |    `create_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
        |    `update_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
        |    `expiration_time` TIMESTAMP    NULL     DEFAULT NULL,
        |    PRIMARY KEY (`id`),
        |    UNIQUE INDEX `idx_name` (`name` ASC) VISIBLE,
        |    UNIQUE INDEX `idx_path` (`path` ASC) VISIBLE,
        |    UNIQUE INDEX `idx_url` (`url` ASC) VISIBLE
        |);
        |""".stripMargin)
    createTableStatement.execute()
    mySqlContainer
  }

  "ServiceActions" should {

    val serviceActions = MySqlServiceActions()

    def createDataSource(container: Containers) = {
      val config = new HikariConfig()
      config.setPoolName(container.databaseName)
      config.setDriverClassName(container.driverClassName)
      config.setJdbcUrl(container.jdbcUrl)
      config.setUsername(container.username)
      config.setPassword(container.password)
      new HikariDataSource(config)
    }

    "insert non-existing values" in withContainers { mysqlContainer =>
      val dataSource = createDataSource(mysqlContainer)
      val db = JdbcBackend.Database.forDataSource(ds = dataSource, None)


      val service = Iterator.continually(serviceGen.sample).flatten.toSeq.head
      db.run(serviceActions.upsert(service)).futureValue
      db.run(serviceActions.getAll).futureValue shouldBe Seq(service)

      dataSource.close()
    }

    "update existing values" in withContainers { mysqlContainer =>
      val dataSource = createDataSource(mysqlContainer)
      val db = JdbcBackend.Database.forDataSource(ds = dataSource, None)

      val service = Iterator.continually(serviceGen.sample).flatten.toSeq.head
      db.run(serviceActions.upsert(service)).futureValue
      val updatedService = service.copy(name = service.name + "-1")
      db.run(serviceActions.upsert(updatedService)).futureValue

      db.run(serviceActions.getAll).futureValue shouldBe Seq(updatedService)

      dataSource.close()
    }

    "delete values" in withContainers { mysqlContainer =>
      val dataSource = createDataSource(mysqlContainer)
      val db = JdbcBackend.Database.forDataSource(ds = dataSource, None)

      val service = Iterator.continually(serviceGen.sample).flatten.toSeq.head
      db.run(serviceActions.upsert(service)).futureValue
      db.run(serviceActions.delete(service.id)).futureValue

      db.run(serviceActions.getAll).futureValue shouldBe Seq.empty

      dataSource.close()
    }
  }
}
