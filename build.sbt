name := "mocker"

version := "0.1"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test
  )
  .aggregate(schemaRegistry, common, gateway, mq, rest)

lazy val schemaRegistry = (project in file("schema-registry"))

lazy val common = (project in file("common"))
  .dependsOn(schemaRegistry)
  .settings(
    jacocoReportSettings := JacocoReportSettings()
      .withThresholds(
        JacocoThresholds(
          instruction = 0,
          method = 0,
          branch = 0,
          complexity = 0,
          line = 0,
          clazz = 0
        )
      ),
    Test / jacocoExcludes := Seq(
      "com.mocker*"
    )
  )

lazy val gateway = (project in file("gateway"))
  .dependsOn(common)

lazy val mq = (project in file("mq"))
  .dependsOn(common)

lazy val rest = (project in file("rest"))
  .dependsOn(common)
