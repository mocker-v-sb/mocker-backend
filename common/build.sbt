import Dependencies._

name := "common"

libraryDependencies ++= (logging ++ slick :+ typesafeConfig)
