import scoverage.ScoverageKeys

name := "mocker"

version := "0.1"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test
  )
  .aggregate(schemaRegistry, common, gateway, mq, restApi)

lazy val schemaRegistry = (project in file("schema-registry"))

lazy val common = (project in file("common"))
  .dependsOn(schemaRegistry)
  .settings(
    ScoverageKeys.coverageEnabled := true,
    ScoverageKeys.coverageMinimumStmtTotal := 50,
    ScoverageKeys.coverageFailOnMinimum := true
  )

lazy val gateway = (project in file("gateway"))
  .dependsOn(common)
  .settings(
    ScoverageKeys.coverageEnabled := true,
    ScoverageKeys.coverageMinimumStmtTotal := 50,
    ScoverageKeys.coverageFailOnMinimum := true
  )

lazy val mq = (project in file("mq"))
  .settings(
    ScoverageKeys.coverageEnabled := true,
    ScoverageKeys.coverageMinimumStmtTotal := 50,
    ScoverageKeys.coverageFailOnMinimum := true
  )
  .dependsOn(common)

lazy val restApi = (project in file("rest/api"))
  .dependsOn(common)
  .settings(
    ScoverageKeys.coverageEnabled := true,
    ScoverageKeys.coverageMinimumStmtTotal := 50,
    ScoverageKeys.coverageFailOnMinimum := true
  )
