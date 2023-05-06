import Dependencies._

name := "mq"

libraryDependencies ++= (coreZio ++ kafka ++ rabbitmq ++ grpc)
