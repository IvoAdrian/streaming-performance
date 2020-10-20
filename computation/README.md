To run the experiments shells scripts have been created. 
They can be found in the subfolders: [akka/bin](akka/bin) and [reactor/bin](reactor/bin) .

The whole-benchmark-evaluation.sh is the main script, which calls the other scripts. It runs the application for more than 100 seconds/time windows and prints out the latency, CPU usage and memory usage every second.

It needs two parameters to run:
* the execution platform (jvm, graalvm or native)
* the throughput/event generation rate

