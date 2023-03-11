import sbt._

object Dependencies {

  lazy val coreZio = Seq(
    "dev.zio" %% "zio" % "2.0.8",
    "dev.zio" %% "zio-json" % "0.4.2",
    "io.d11" %% "zhttp" % "2.0.0-RC10"
  )

  lazy val logging = Seq(
    "org.slf4j" % "slf4j-api" % "2.0.5",
    "ch.qos.logback" % "logback-classic" % "1.4.5"
  )

  lazy val kafka = Seq(
    "dev.zio" %% "zio-streams" % "2.0.8",
    "dev.zio" %% "zio-kafka" % "2.0.7"
  )

  lazy val typesafeConfig = "com.typesafe" % "config" % "1.4.2"

  lazy val grpc = Seq(
    "io.grpc" % "grpc-netty" % "1.53.0",
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
  )

  lazy val slick = Seq(
    "com.typesafe.slick" %% "slick" % "3.4.1",
    "io.scalac" %% "zio-slick-interop" % "0.6.0"
  )

  object TestContainers {
    lazy val testcontainersScalaVersion = "0.40.12"

    lazy val libraries = Seq(
      "com.dimafeng" %% "testcontainers-scala-scalatest" % testcontainersScalaVersion,
      "com.dimafeng" %% "testcontainers-scala-mysql" % testcontainersScalaVersion
    )
  }

  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.17.0"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.15"

  lazy val mySqlConnector = "mysql" % "mysql-connector-java" % "8.0.32"

}
