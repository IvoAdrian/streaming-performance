:
# Runs the Computation main program with the GraalVM Native Image Agent
set -ue
buildFile=build.sbt
test -f $buildFile || { echo "You must start this script with $buildFile in working directory"; exit 1; }
nativeImageConfDir=target/scala-2.13/classes/META-INF/native-image
ivyCache="$HOME/.ivy2/cache"

mkdir -p $nativeImageConfDir
#                                                                      -verbose:class 
java -agentlib:native-image-agent=config-merge-dir=$nativeImageConfDir -Dfile.encoding=UTF-8 -classpath "/home/ivo-k/github/computation-akka/target/scala-2.13/classes:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/config/1.4.0/config-1.4.0.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/vmencik/graal-akka-stream_2.13/0.5.0/graal-akka-stream_2.13-0.5.0.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/vmencik/graal-akka-slf4j_2.13/0.5.0/graal-akka-slf4j_2.13-0.5.0.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/vmencik/graal-akka-http_2.13/0.5.0/graal-akka-http_2.13-0.5.0.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/vmencik/graal-akka-actor_2.13/0.5.0/graal-akka-actor_2.13-0.5.0.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-parser-combinators_2.13/1.1.2/scala-parser-combinators_2.13-1.1.2.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-java8-compat_2.13/0.9.0/scala-java8-compat_2.13-0.9.0.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.2/scala-library-2.13.2.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/reactivestreams/reactive-streams/1.0.3/reactive-streams-1.0.3.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-stream_2.13/2.6.5/akka-stream_2.13-2.6.5.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-protobuf-v3_2.13/2.6.5/akka-protobuf-v3_2.13-2.6.5.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-actor_2.13/2.6.5/akka-actor_2.13-2.6.5.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/ssl-config-core_2.13/0.4.1/ssl-config-core_2.13-0.4.1.jar" Main

