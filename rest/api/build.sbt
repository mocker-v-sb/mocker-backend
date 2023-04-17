import Dependencies._

name := "rest-api"

libraryDependencies ++= coreZio ++ grpc ++ slick :+ mySqlConnector
