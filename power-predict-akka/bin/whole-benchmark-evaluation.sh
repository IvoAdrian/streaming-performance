#!/bin/bash
# $1 = The platform (jvm or native)
# $2 = The input rate (e.g. 1000, 8000)
# a simple script for the evaluation of the Akka Benchmark for a given input rate in the stream-bench.sh file
set -o errexit -o nounset

export PLATFORM=$1
export LOAD=$2

# Be in the directory: ~/github/power-predict-akka

export projectDir=~/github/power-predict-akka

pushd ../power-predict-tcp-generator

. bin/run-generator.sh $LOAD

sleep 3
popd

case "$PLATFORM" in 
	jvm) . bin/run-jvm.sh $LOAD > Benchmark_Results/$PLATFORM$LOAD-latencies.txt;;
	graalvm) . bin/run-graalvm.sh $LOAD > Benchmark_Results/$PLATFORM$LOAD-latencies.txt;;
	native) target/graalvm-native-image/power-predict-akka $LOAD > Benchmark_Results/$PLATFORM$LOAD-latencies.txt &
	export RUN_PID=$!
	echo native image started with PID $RUN_PID;;
	*)
	echo Wrong or no platform $PLATFORM
	exit 1;;
esac


ps -F $RUN_PID

sleep 2


timeout 120 top -b -d 1 -p $RUN_PID > Benchmark_Results/top-results.txt  || echo timeout finished with exit-code $?

ps -F $RUN_PID
echo Now killing process with PID $RUN_PID
kill $RUN_PID
ps -F $GEN_PID
echo Now killing process with PID $GEN_PID
kill $GEN_PID
sleep 3

# In Calc Datei importieren

grep ivo-k Benchmark_Results/top-results.txt > Benchmark_Results/$PLATFORM$LOAD-CPUMemory.txt

sh bin/extract-CPU+Memory.sh Benchmark_Results/$PLATFORM$LOAD-CPUMemory.txt 

# In Calc Datei importieren
echo Schlafen 10 sec...
sleep 10

echo "Ende" 
exit

