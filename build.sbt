import com.typesafe.sbt.packager.docker.Cmd

name := "mocker"

version := "0.1"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .aggregate(schemaRegistry, common, gateway, mq, restApi, restCore)

lazy val schemaRegistry = project in file("schema-registry")

lazy val common = (project in file("common"))
  .dependsOn(schemaRegistry)

lazy val gateway = (project in file("gateway"))
  .dependsOn(common)
  .settings(
    dockerExposedPorts += 9000,
    dockerBaseImage := "amazoncorretto:17-alpine-jdk",
    packageName := "gateway-server",
    dockerCommands := dockerCommands.value.flatMap {
      case cmd @ Cmd("FROM", _) => List(cmd, Cmd("RUN", "apk update && apk add bash"))
      case other                => List(other)
    }
  )
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(AshScriptPlugin)

lazy val mq = (project in file("mq"))
  .dependsOn(common)
  .settings(
    dockerExposedPorts += 8888,
    dockerBaseImage := "amazoncorretto:17-alpine-jdk",
    packageName := "mq-mocker-server",
    dockerCommands := dockerCommands.value.flatMap {
      case cmd @ Cmd("FROM", _) => List(cmd, Cmd("RUN", "apk update && apk add bash"))
      case other                => List(other)
    }
  )
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(AshScriptPlugin)

lazy val restApi = (project in file("rest/api"))
  .dependsOn(restCore % "compile->compile;test->test")
  .settings(
    dockerExposedPorts += 8889,
    dockerBaseImage := "amazoncorretto:17-alpine-jdk",
    packageName := "rest-mocker-server",
    dockerCommands := dockerCommands.value.flatMap {
      case cmd @ Cmd("FROM", _) => List(cmd, Cmd("RUN", "apk update && apk add bash"))
      case other                => List(other)
    }
  )
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(AshScriptPlugin)

lazy val restCore = (project in file("rest/core"))
  .dependsOn(common)
