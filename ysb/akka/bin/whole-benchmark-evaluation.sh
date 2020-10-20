#!/bin/bash
# $1 = The platform (jvm or native)
# $2 = The event generation rate (e.g. 1000, 8000)
# a simple script for the evaluation of the Akka Benchmark for a given event generation rate in the stream-bench.sh file
set -o errexit -o nounset

export PLATFORM=$1
export LOAD=$2

# Be in the directory: ~/github/ysb-akka

export ysbDir=~/github/yahoo-streaming-benchmarks

pushd $ysbDir
bash stream-bench.sh STOP_LOAD
bash stream-bench.sh STOP_REDIS
bash stream-bench.sh STOP_KAFKA
bash stream-bench.sh STOP_ZK
sleep 3
echo starting zookeeper...
bash stream-bench.sh START_ZK >ZK.log
echo starting kafka...
bash stream-bench.sh START_KAFKA > KAFKA.log
echo starting Redis...
bash stream-bench.sh START_REDIS > Redis.log
sleep 2

popd 
case "$PLATFORM" in 
	jvm) . bin/run-jvm.sh;;
	graalvm) . bin/run-graalvm.sh;;
	native) target/graalvm-native-image/akka-streaming-benchmark &
	export YSB_RUN_PID=$!
	echo native image started with PID $YSB_RUN_PID;;
	*)
	echo Wrong or no platform $PLATFORM
	exit 1;;
esac


ps -F $YSB_RUN_PID

sleep 2

pushd $ysbDir
bash stream-bench.sh START_LOAD  # hier muss die input rate noch übergeben werden
popd

timeout 120 top -b -d 1 -p $YSB_RUN_PID > Benchmark_Results/top-results.txt  || echo timeout finished with exit-code $?
ps -F $YSB_RUN_PID
echo Now killing process with PID $YSB_RUN_PID
kill $YSB_RUN_PID

sleep 10
pushd $ysbDir
bash stream-bench.sh STOP_LOAD
popd
sleep 3

sh ~/github/ysb-eval-util/bin/run-evaluation-latency.sh > Benchmark_Results/$PLATFORM$LOAD-latencies.txt

grep ivo-k Benchmark_Results/top-results.txt > Benchmark_Results/$PLATFORM$LOAD-CPUMemory.txt

sh bin/extract-CPU+Memory.sh Benchmark_Results/$PLATFORM$LOAD-CPUMemory.txt 

echo sleep 10 sec...
sleep 10

pushd $ysbDir
bash stream-bench.sh STOP_REDIS
bash stream-bench.sh STOP_KAFKA
bash stream-bench.sh STOP_ZK

echo "Ende" 
exit

