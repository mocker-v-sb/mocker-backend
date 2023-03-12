import scoverage.ScoverageKeys

name := "mocker"

version := "0.1"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .aggregate(schemaRegistry, common, gateway, mq, restApi, restCore)

lazy val schemaRegistry = (project in file("schema-registry"))

lazy val common = (project in file("common"))
  .dependsOn(schemaRegistry)
  .settings(
    ScoverageKeys.coverageEnabled := true,
    ScoverageKeys.coverageMinimumStmtTotal := 0,
    ScoverageKeys.coverageFailOnMinimum := true
  )

lazy val gateway = (project in file("gateway"))
  .dependsOn(common)
  .settings(
    ScoverageKeys.coverageEnabled := true,
    ScoverageKeys.coverageMinimumStmtTotal := 0,
    ScoverageKeys.coverageFailOnMinimum := true
  )

lazy val mq = (project in file("mq"))
  .settings(
    ScoverageKeys.coverageEnabled := true,
    ScoverageKeys.coverageMinimumStmtTotal := 0,
    ScoverageKeys.coverageFailOnMinimum := true
  )
  .dependsOn(common)

lazy val restApi = (project in file("rest/api"))
  .dependsOn(restCore % "compile->compile;test->test")
  .settings(
    ScoverageKeys.coverageEnabled := true,
    ScoverageKeys.coverageMinimumStmtTotal := 0,
    ScoverageKeys.coverageFailOnMinimum := true
  )

lazy val restCore = (project in file("rest/core"))
  .dependsOn(common)
  .settings(
    ScoverageKeys.coverageEnabled := true,
    ScoverageKeys.coverageMinimumStmtTotal := 0,
    ScoverageKeys.coverageFailOnMinimum := true
  )
