

To run the experiments shells scripts have been created. They can be found in the subfolders: [akka/bin](akka/bin) and [reactor/bin](reactor/bin) .

The `run-with-agent.sh` script was used to set up the configuration for Native Image.

The `whole-benchmark-evaluation.sh` is the main script, which calls the other scripts. It runs the application for more than 100 seconds/time windows and prints out the latency, CPU usage and memory usage every second into txt files.

It needs two parameters to run:
1. the execution platform (`jvm`, `graalvm` or `native`)
2. the throughput

To reproduce the experiments, you have to adapt the paths in the scripts to your local configuration.
