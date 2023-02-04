name := "schema-registry"

version := "0.1"

scalaVersion := "2.13.10"

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value
)
