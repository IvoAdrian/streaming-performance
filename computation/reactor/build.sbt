name := "computation-reactor"

organization := "de.knabe"
scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "io.projectreactor" % "reactor-core" % "3.3.7.RELEASE",
  "io.projectreactor" %% "reactor-scala-extensions" % "0.7.0",
  //for usage of SubstrateVM annotations:
  "org.graalvm.nativeimage" % "svm" % "20.0.0" % "provided"
)

enablePlugins(GraalVMNativeImagePlugin)

graalVMNativeImageOptions ++= Seq(
  "-H:+TraceClassInitialization",
  "-H:IncludeResources=.*\\.properties",
  "--initialize-at-build-time",
  "--no-fallback",
  "--allow-incomplete-classpath",
  "--report-unsupported-elements-at-runtime",
  "--verbose",
  "-H:+ReportExceptionStackTraces"
)
