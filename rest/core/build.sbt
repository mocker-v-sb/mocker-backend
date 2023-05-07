import Dependencies._

name := "rest-core"

lazy val testDependencies =
  (TestContainers.libraries :+ mySqlConnector :+ scalaTest).map(_ % Test)

libraryDependencies ++= coreZio ++ slick ++ testDependencies :+ zioRedis :+ "dev.zio" %% "zio-schema-protobuf" % "0.4.10" :+ "dev.zio" %% "zio-schema" % "0.4.10" :+ "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.5",
  "io.circe" %% "circe-generic" % "0.14.5",
  "io.circe" %% "circe-generic-extras" % "0.14.3",
  "io.circe" %% "circe-parser" % "0.14.5"
) :+ apacheAvro
