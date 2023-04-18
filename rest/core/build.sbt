import Dependencies._

name := "rest-core"

lazy val testDependencies =
  (TestContainers.libraries :+ mySqlConnector :+ scalaTest).map(_ % Test)

libraryDependencies ++= coreZio ++ testDependencies
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.5",
  "io.circe" %% "circe-generic" % "0.14.5",
  "io.circe" %% "circe-generic-extras" % "0.14.3",
  "io.circe" %% "circe-parser" % "0.14.5"
) :+ apacheAvro
libraryDependencies ++= slick
