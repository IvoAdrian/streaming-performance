The applications have been compiled with `sbt compile` (for GraalVM and HotSpot JVM) in the directories which contain `build.sbt`.

To run the experiments shells scripts have been created. 
They can be found in the subfolders: [akka/bin](akka/bin) and [reactor/bin](reactor/bin).

The `run-with-agent.sh` script was used after compilation to set up the configuration for Native Image.

After that, the Native Image of the program was built by `sbt graalvm-native-image:packageBin`.

The `whole-benchmark-evaluation.sh` is the main script, which calls the other scripts. It runs the application for more than 100 seconds/time windows and prints out the latency, CPU usage and memory usage every second into .txt files. These .txt files will be created in the `Benchmark_Results` directory, which will be created parallel to the `bin` directory.

It needs one parameter to run:
* the execution platform (`jvm`, `graalvm` or `native`)


To reproduce the experiments, you have to adapt the paths in the scripts to your local configuration.


