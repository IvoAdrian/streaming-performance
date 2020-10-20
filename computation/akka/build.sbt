
name := "computation-akka"

version := "0.1"

scalaVersion := "2.13.2"
val graalAkkaVersion = "0.5.0"
val AkkaVersion = "2.5.23"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.github.vmencik" %% "graal-akka-http" % graalAkkaVersion,
  "com.github.vmencik" %% "graal-akka-slf4j" % graalAkkaVersion,
  "org.graalvm.nativeimage" % "svm" % "20.0.0" % "provided"
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
  "--allow-incomplete-classpath"
  , "--report-unsupported-elements-at-runtime"
  , "--verbose"
  , "-H:+ReportExceptionStackTraces"
)
