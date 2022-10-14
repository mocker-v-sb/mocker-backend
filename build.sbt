name := "mocker"

version := "0.1"

scalaVersion := "2.13.10"

lazy val gateway = (project in file("gateway"))

lazy val rest = (project in file("rest"))

lazy val mq = (project in file("mq"))

lazy val graphql = (project in file("graphql"))
