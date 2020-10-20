name := "ysb-akka"

version := "0.1"
scalaVersion := "2.12.3"

val graalAkkaVersion = "0.5.0"
val AkkaVersion = "2.5.23"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.13",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "org.slf4j" % "slf4j-simple" % "1.7.30",
  "io.spray" %%  "spray-json" % "1.3.5",
  "com.github.etaty" %% "rediscala" % "1.9.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.github.vmencik" %% "graal-akka-http" % graalAkkaVersion,
  "com.github.vmencik" %% "graal-akka-slf4j" % graalAkkaVersion
  //for usage of SubstrateVM annotations:
  , "org.graalvm.nativeimage" % "svm" % "20.0.0" % "provided"

)

enablePlugins(GraalVMNativeImagePlugin)

graalVMNativeImageOptions ++= Seq(
  "-H:IncludeResources=.*\\.properties",
  s"-H:ReflectionConfigurationFiles=${baseDirectory.value}/graal/reflectconf-jul.json,${baseDirectory.value}/graal/reflectconf-benchmark.json",
  "--initialize-at-build-time",
  "--initialize-at-run-time=" +
    "akka.protobuf.DescriptorProtos," +
    "com.typesafe.config.impl.ConfigImpl$EnvVariablesHolder," +
    "com.typesafe.config.impl.ConfigImpl$SystemPropertiesHolder",
  "--no-fallback",
  "--allow-incomplete-classpath"
  //2020-05-18 Knabe
  , "--report-unsupported-elements-at-runtime"
  , "--verbose"
  , "-H:+ReportExceptionStackTraces"
)


