name := "mocker"

version := "0.1"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .aggregate(schemaRegistry, common, gateway, mq, rest)

lazy val schemaRegistry = (project in file("schema-registry"))

lazy val common = (project in file("common"))
  .dependsOn(schemaRegistry)

lazy val gateway = (project in file("gateway"))
  .dependsOn(common)

lazy val mq = (project in file("mq"))
  .dependsOn(common)

lazy val rest = (project in file("rest"))
  .dependsOn(common)
