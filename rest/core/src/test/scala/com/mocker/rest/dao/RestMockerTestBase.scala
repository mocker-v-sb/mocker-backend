package com.mocker.rest.dao

import com.dimafeng.testcontainers.MySQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.mocker.common.utils.SqlScript
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.testcontainers.utility.DockerImageName
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend
import slick.jdbc.MySQLProfile.api._

trait RestMockerTestBase extends BeforeAndAfterEach with TestContainerForAll with ScalaFutures {
  self: Suite =>

  protected var db: Option[JdbcBackend.DatabaseDef] = None

  override val containerDef: MySQLContainer.Def = MySQLContainer.Def(
    dockerImageName = DockerImageName.parse("mysql:8.0.31"),
    databaseName = "rest_mocker"
  )

  override def startContainers(): MySQLContainer = {
    val mySqlContainer = super.startContainers()
    db = Some(
      JdbcBackend.Database.forURL(
        url = mySqlContainer.container.getJdbcUrl,
        user = mySqlContainer.container.getUsername,
        password = mySqlContainer.container.getPassword
      )
    )

    mySqlContainer
  }

  override def beforeEach(): Unit = {
    val path = "sql/schema.sql"
    val action = SqlScript.statementsFromFile(path) match {
      case Some(statements) => DBIO.sequence(statements.map(s => sqlu"#$s"))
      case None             => DBIO.failed(new RuntimeException(s"Can't load script at $path"))
    }
    db.foreach(_.run(action).futureValue)
  }

  override def afterEach(): Unit = {
    val path = "sql/deleteTables.sql"
    val action = SqlScript.statementsFromFile(path) match {
      case Some(statements) => DBIO.sequence(statements.map(s => sqlu"#$s"))
      case None             => DBIO.failed(new RuntimeException(s"Can't load script at $path"))
    }
    db.foreach(_.run(action).futureValue)
  }
}
