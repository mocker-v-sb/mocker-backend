import scoverage.ScoverageKeys

name := "mocker"

version := "0.1"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test,
    ScoverageKeys.coverageMinimumStmtTotal := 100,
    ScoverageKeys.coverageFailOnMinimum := false
  )
  .aggregate(schemaRegistry, common, gateway, mq, restApi)

lazy val schemaRegistry = (project in file("schema-registry"))

lazy val common = (project in file("common"))
  .dependsOn(schemaRegistry)

lazy val gateway = (project in file("gateway"))
  .dependsOn(common)

lazy val mq = (project in file("mq"))
  .dependsOn(common)

lazy val restApi = (project in file("rest/api"))
  .dependsOn(common)
