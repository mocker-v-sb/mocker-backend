import Dependencies._

name := "common"

libraryDependencies ++= (logging ++ otel :+ typesafeConfig :+ scalaCheck)
