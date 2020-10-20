The applications have been compiled with `sbt compile` (for GraalVM and HotSpot JVM) in the directories which contain `build.sbt`.



To run the experiments shells scripts have been created. They can be found in the subfolders: [akka/bin](akka/bin) and [reactor/bin](reactor/bin) .

The `run-with-agent.sh` script was used to set up the configuration for Native Image. It was only necessary for the Reactor variant. For Akka preconfigured libraries were included in the `build.sbt`.

After that, the Native Image of the program was built by `sbt graalvm-native-image:packageBin`.


The `whole-benchmark-evaluation.sh` is the main script, which calls the other scripts. It also calls the YSB-Evaluation tool, found in [evaluation-tool](evaluation-tool). `whole-benchmark-evaluation.sh` runs the application for more than 100 seconds/time windows and prints out the latency, CPU usage and memory usage every second into txt files. These .txt files will be created in the `Benchmark_Results` directory, which will be created parallel to the `bin` directory.

It needs two parameters to run:
1. the execution platform (`jvm`, `graalvm` or `native`)
2. the event generation rate

The original YSB generator was used. To use it you have to clone the original [YSB directory](https://github.com/yahoo/streaming-benchmarks). After that, you replace the `stream-bench.sh` file with the adapted file [generator/stream-bench.sh](generator/stream-bench.sh).

To reproduce the experiments, you have to adapt the paths in the scripts to your local configuration.

