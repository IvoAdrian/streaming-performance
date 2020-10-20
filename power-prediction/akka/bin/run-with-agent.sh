:
# Runs the Power Prediction main program with the GraalVM Native Image Agent
set -ue
buildFile=build.sbt
test -f $buildFile || { echo "You must start this script with $buildFile in working directory"; exit 1; }
nativeImageConfDir=target/scala-2.12/classes/META-INF/native-image
ivyCache="$HOME/.ivy2/cache"

mkdir -p $nativeImageConfDir
#                                                                      -verbose:class 
java -agentlib:native-image-agent=config-merge-dir=$nativeImageConfDir -Dfile.encoding=UTF-8 -classpath "/home/ivo-k/github/power-predict-reactor/target/scala-2.12/classes:$ivyCache/io.projectreactor/reactor-scala-extensions_2.12/jars/reactor-scala-extensions_2.12-0.7.0.jar:$ivyCache/org.reactivestreams/reactive-streams/jars/reactive-streams-1.0.3.jar:$ivyCache/org.scala-lang/scala-library/jars/scala-library-2.12.8.jar:$ivyCache/org.scala-lang.modules/scala-collection-compat_2.12/jars/scala-collection-compat_2.12-2.1.6.jar:$ivyCache/io.netty/netty-buffer/jars/netty-buffer-4.1.51.Final.jar:$ivyCache/io.netty/netty-codec/jars/netty-codec-4.1.51.Final.jar:$ivyCache/io.netty/netty-codec-http/jars/netty-codec-http-4.1.51.Final.jar:$ivyCache/io.netty/netty-codec-http2/jars/netty-codec-http2-4.1.51.Final.jar:$ivyCache/io.netty/netty-codec-socks/jars/netty-codec-socks-4.1.51.Final.jar:$ivyCache/io.netty/netty-common/jars/netty-common-4.1.51.Final.jar:$ivyCache/io.netty/netty-handler/jars/netty-handler-4.1.51.Final.jar:$ivyCache/io.netty/netty-handler-proxy/jars/netty-handler-proxy-4.1.51.Final.jar:$ivyCache/io.netty/netty-resolver/jars/netty-resolver-4.1.51.Final.jar:$ivyCache/io.netty/netty-transport/jars/netty-transport-4.1.51.Final.jar:$ivyCache/io.netty/netty-transport-native-epoll/jars/netty-transport-native-epoll-4.1.51.Final-linux-x86_64.jar:$ivyCache/io.netty/netty-transport-native-unix-common/jars/netty-transport-native-unix-common-4.1.51.Final.jar:$ivyCache/io.projectreactor/reactor-core/jars/reactor-core-3.3.8.RELEASE.jar:$ivyCache/io.projectreactor.netty/reactor-netty/jars/reactor-netty-0.9.10.RELEASE.jar" de.knabe.ReactorPowerApp
