name := "mocker"

version := "0.1"

scalaVersion := "2.13.10"

lazy val zioDependencies = Seq(
  "dev.zio" %% "zio" % "2.0.6",
  "dev.zio" %% "zio-streams" % "2.0.6",
  "dev.zio" %% "zio-json" % "0.4.2",
  "io.d11" %% "zhttp" % "2.0.0-RC10"
)

lazy val scalaCommonDependencies = Seq(
  "com.typesafe" % "config" % "1.4.2",
  "org.slf4j" % "slf4j-api" % "2.0.5",
  "ch.qos.logback" % "logback-classic" % "1.4.5",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
)

lazy val common = (project in file("common"))
  .settings {
    libraryDependencies ++= scalaCommonDependencies
  }
  .dependsOn(schemaRegistry)

lazy val gateway = (project in file("gateway"))
  .dependsOn(common)
  .settings(libraryDependencies ++= zioDependencies)

lazy val mq = (project in file("mq"))

lazy val rest = (project in file("rest"))
  .dependsOn(common)

lazy val graphql = (project in file("graphql"))

lazy val schemaRegistry = (project in file("schema-registry"))

lazy val root = (project in file("."))
  .aggregate(schemaRegistry, common, gateway, mq, graphql, rest)
