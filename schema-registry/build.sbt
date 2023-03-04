Compile / PB.targets := Seq(
  scalapb.gen(grpc = true) -> (Compile / sourceManaged).value / "scalapb",
  scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value / "scalapb"
)

Compile / PB.protoSources := Seq(
  (ThisBuild / baseDirectory).value / "schema-registry" / "src" / "main" / "protobuf"
)

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %%% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
)
