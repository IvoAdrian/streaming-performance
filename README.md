# Stream Processing Performance Experiments - JVM and GraalVM vs Native Image

Material for the bachelor's thesis "Measuring the Performance Impact of Bytecode Execution in Multi-Core Stream Processing Environments"



In this GitHub repository the source code of the experiments from the mentioned thesis is laid out. 

There are the three following experiments:

* the Yahoo Streaming Benchmarks
* the Power Prediction Application
* the Computation Application



The folders with their respective names can be seen above. Each experiment was realized in an Akka Streams and  a Project Reactor variant - each has their respective subfolder. In these subfolders the instructions of how to execute the experiment can be found.

Each variant was executed on the HotSpot JVM, GraalVM and GraalVM Native Image.