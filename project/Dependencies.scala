import sbt._

object Dependencies {

  lazy val zio = Seq(
    "dev.zio" %% "zio" % "2.0.6",
    "dev.zio" %% "zio-streams" % "2.0.6",
    "dev.zio" %% "zio-json" % "0.4.2",
    "io.d11" %% "zhttp" % "2.0.0-RC10"
  )

  lazy val logging = Seq(
    "org.slf4j" % "slf4j-api" % "2.0.5",
    "ch.qos.logback" % "logback-classic" % "1.4.5"
  )

  lazy val typesafeConfig = "com.typesafe" % "config" % "1.4.2"

  lazy val scalapbRuntime = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
}
