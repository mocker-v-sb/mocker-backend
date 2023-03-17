import Dependencies._

name := "rest-core"

lazy val testDependencies =
  (TestContainers.libraries :+ mySqlConnector :+ scalaTest).map(_ % Test)

libraryDependencies ++= coreZio ++ testDependencies
