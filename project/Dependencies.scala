import sbt._

object Dependencies {

  lazy val coreZio = Seq(
    "dev.zio" %% "zio" % "2.0.10",
    "dev.zio" %% "zio-json" % "0.4.2"
  )

  lazy val zioHttp = Seq(
    "dev.zio" %% "zio-http" % "0.0.5",
    "com.github.jwt-scala" %% "jwt-core" % "9.2.0"
  )

  lazy val zioPostgres = Seq(
    "dev.zio" %% "zio-sql-postgres" % "0.1.2"
  )

  lazy val logging = Seq(
    "org.slf4j" % "slf4j-api" % "2.0.5",
    "ch.qos.logback" % "logback-classic" % "1.4.6"
  )

  lazy val kafka = Seq(
    "dev.zio" %% "zio-streams" % "2.0.10",
    "dev.zio" %% "zio-kafka" % "2.1.3"
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

  lazy val scalaMock = "org.scalamock" %% "scalamock" % "5.2.0"

  lazy val apacheAvro = "org.apache.avro" % "avro" % "1.11.0"

  lazy val zioConfig = Seq(
    "dev.zio" %% "zio-config" % "3.0.7",
    "dev.zio" %% "zio-config-typesafe" % "3.0.7",
    "dev.zio" %% "zio-config-magnolia" % "3.0.7"
  )
}
