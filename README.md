# Stream Processing Performance Experiments - Bytecode vs Native Execution

This repository contains the source code of the applications for the experiments of the bachelor's thesis "Measuring the Performance Impact of Bytecode Execution in Multi-Core Stream Processing Environments".

Furthermore it contains the spreadsheets with the results of the runs of the experiments.

There are the three following experiments:

* the [Yahoo Streaming Benchmarks](ysb)
* the Power Prediction Application
* the Computation Application

The folders with their respective names can be seen above. Each experiment was realized in an Akka Streams and  a Project Reactor variant - each has their respective subfolder. In these subfolders the instructions of how to execute the experiment can be found.

Each variant was executed on the HotSpot JVM, GraalVM and GraalVM Native Image.