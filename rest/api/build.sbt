import Dependencies._

name := "rest-api"

libraryDependencies ++= coreZio ++ grpc :+ (scalaTest % Test) :+ (scalaMock % Test)
