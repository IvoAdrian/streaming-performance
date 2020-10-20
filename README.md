# Stream Processing Performance Experiments - Bytecode vs Native Execution

This repository contains the source code of the applications for the experiments of the bachelor's thesis "Measuring the Performance Impact of Bytecode Execution in Multi-Core Stream Processing Environments".

Furthermore it contains the spreadsheets with the results of the runs of the experiments in a LibreOffice Calc format.

There are the three following experiments:

* the [Yahoo Streaming Benchmarks](ysb)
* the [Power Prediction Application](power-prediction)
* the [Computation Application](computation)

In these experiment folders, the instructions of how to carry out the experiment can be found.

Each experiment was implemented in an Akka Streams and a Project Reactor variant.

Each variant was executed on the HotSpot JVM, GraalVM and GraalVM Native Image.
