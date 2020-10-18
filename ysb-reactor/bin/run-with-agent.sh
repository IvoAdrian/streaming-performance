:
# Runs the Consumer main program with the GraalVM Native Agent
set -ue
buildFile=build.sbt
test -f $buildFile || { echo "You must start this script with $buildFile in working directory"; exit 1; }
nativeImageConfDir=target/scala-2.13/classes/META-INF/native-image
ivyCache="$HOME/.ivy2/cache"

mkdir -p $nativeImageConfDir
#                                                                      -verbose:class 
java -agentlib:native-image-agent=config-merge-dir=$nativeImageConfDir -classpath /home/ivo-k/github/ysb-reactor/target/scala-2.13/classes:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.3/scala-library-2.13.3.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-stm/scala-stm_2.13/0.9.1/scala-stm_2.13-0.9.1.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-java8-compat_2.13/0.9.0/scala-java8-compat_2.13-0.9.0.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-actor_2.13/2.5.23/akka-actor_2.13-2.5.23.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/config/1.3.3/config-1.3.3.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/etaty/rediscala_2.13/1.9.0/rediscala_2.13-1.9.0.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.30/slf4j-simple-1.7.30.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/io/projectreactor/reactor-core/3.3.7.RELEASE/reactor-core-3.3.7.RELEASE.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/io/projectreactor/reactor-scala-extensions_2.13/0.7.0/reactor-scala-extensions_2.13-0.7.0.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/io/projectreactor/kafka/reactor-kafka/1.1.0.RELEASE/reactor-kafka-1.1.0.RELEASE.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/apache/kafka/kafka-clients/2.0.0/kafka-clients-2.0.0.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/lz4/lz4-java/1.4.1/lz4-java-1.4.1.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/reactivestreams/reactive-streams/1.0.3/reactive-streams-1.0.3.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-collection-compat_2.13/2.1.6/scala-collection-compat_2.13-2.1.6.jar:/home/ivo-k/.cache/coursier/v1/https/repo1.maven.org/maven2/org/xerial/snappy/snappy-java/1.1.7.1/snappy-java-1.1.7.1.jar Main
