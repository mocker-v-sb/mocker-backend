import Dependencies._

name := "rest-api"

libraryDependencies ++= coreZio ++ grpc :+ mySqlConnector :+ apacheAvro
