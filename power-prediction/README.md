The applications have been compiled with `sbt compile` (for GraalVM and HotSpot JVM) in the directories which contain `build.sbt`.


To run the experiments shells scripts have been created. They can be found in the subfolders: [akka/bin](akka/bin) and [reactor/bin](reactor/bin) .

The `run-with-agent.sh` script was used to set up the configuration for Native Image.

After that, the Native Image of the program was built by `sbt graalvm-native-image:packageBin`.


The `whole-benchmark-evaluation.sh` is the main script, which calls the other scripts. It runs the application for more than 100 seconds/time windows and prints out the latency, CPU usage and memory usage every second into txt files.

It needs two arguments to run:
1. the execution platform (`jvm`, `graalvm` or `native`)
2. the throughput

The throughput is used as an argument for the script starting the generator application, which can be found in [generator/bin](generator/bin).

To reproduce the experiments, you have to adapt the paths in the scripts to your local configuration.
