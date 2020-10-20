#!/bin/bash
# $1 = The platform (jvm, graalvm or native)
# a simple script for the evaluation of the Akka Benchmark
set -o errexit -o nounset

export PLATFORM=$1

# Be in the directory: $projectDir

export projectDir=~/github/computation-akka


case "$PLATFORM" in 
	jvm) . bin/run-jvm.sh > Benchmark_Results/$PLATFORM-latencies.txt;;
	graalvm) . bin/run-graalvm.sh > Benchmark_Results/$PLATFORM-latencies.txt;;
	native) target/graalvm-native-image/computation-akka > Benchmark_Results/$PLATFORM-latencies.txt &
	export RUN_PID=$!
	echo native image started with PID $RUN_PID;;
	*)
	echo Wrong or no platform $PLATFORM
	exit 1;;
esac


ps -F $RUN_PID

sleep 2


timeout 100 top -b -d 1 -p $RUN_PID > Benchmark_Results/top-results.txt  || echo timeout finished with exit-code $?

ps -F $RUN_PID
echo Now killing process with PID $RUN_PID
kill $RUN_PID
sleep 3

bash bin/compress-latencies.sh Benchmark_Results/$PLATFORM-latencies.txt Benchmark_Results/$PLATFORM-comprLatencies.txt

grep ivo-k Benchmark_Results/top-results.txt > Benchmark_Results/$PLATFORM-CPUMemory.txt

sh bin/extract-CPU+Memory.sh Benchmark_Results/$PLATFORM-CPUMemory.txt 


# In Calc Datei importieren
echo Schlafen 10 sec...
sleep 5


echo "Ende" 
exit

