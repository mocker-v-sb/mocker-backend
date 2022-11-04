name := "mocker"

version := "0.1"

scalaVersion := "2.13.10"

lazy val zioDependencies = Seq(
  "dev.zio" %% "zio" % "2.0.3",
  "dev.zio" %% "zio-streams" % "2.0.3",
  "dev.zio" %% "zio-json" % "0.3.0-RC8",
  "io.d11"  %% "zhttp" % "2.0.0-RC10",
)

lazy val scalaCommonDependencies = Seq(
  "com.typesafe" % "config" % "1.4.2",
  "org.slf4j" % "slf4j-api" % "2.0.3",
  "ch.qos.logback" % "logback-classic" % "1.4.4",
)

lazy val scalaCore = (project in file("scala-core")).settings {
  libraryDependencies ++= scalaCommonDependencies
}

lazy val gateway = (project in file("gateway"))
  .dependsOn(scalaCore)
  .settings(libraryDependencies ++= zioDependencies)

lazy val mq = (project in file("mq"))

lazy val rest = (project in file("rest"))

lazy val graphql = (project in file("graphql"))
