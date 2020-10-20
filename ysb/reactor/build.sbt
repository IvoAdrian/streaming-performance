
name := "ysb-reactor"

version := "0.1"

scalaVersion := "2.13.3"

val graalAkkaVersion = "0.5.0"


libraryDependencies ++= Seq(
  "io.projectreactor" % "reactor-core" % "3.3.7.RELEASE",
  "io.projectreactor" %% "reactor-scala-extensions" % "0.7.0",
  "io.projectreactor.kafka" % "reactor-kafka" % "1.1.0.RELEASE",
  "org.slf4j" % "slf4j-simple" % "1.7.30",
  "io.spray" %%  "spray-json" % "1.3.5",
  "com.github.etaty" %% "rediscala" % "1.9.0",
  "com.github.vmencik" %% "graal-akka-actor" % graalAkkaVersion
//for usage of SubstrateVM annotations:
  , "org.graalvm.nativeimage" % "svm" % "20.0.0" % "provided"
)

enablePlugins(GraalVMNativeImagePlugin)

graalVMNativeImageOptions ++= Seq(
  "-H:+TraceClassInitialization",
  "-H:IncludeResources=.*\\.properties",
  "--initialize-at-build-time",
  "--initialize-at-run-time=" +
    "akka.protobuf.DescriptorProtos," +
    "com.typesafe.config.impl.ConfigImpl$EnvVariablesHolder," +
    "com.typesafe.config.impl.ConfigImpl$SystemPropertiesHolder",
  "--no-fallback",
  "--allow-incomplete-classpath",
  "--report-unsupported-elements-at-runtime",
  "--verbose",
  "-H:+ReportExceptionStackTraces"
)