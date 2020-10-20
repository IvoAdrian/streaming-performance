package de.knabe

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket
import java.time.LocalTime
import java.util.function.{Function => JavaFunction}

import scala.collection.JavaConverters._
import scala.util.Random

import reactor.core.scala.publisher.SFlux
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers


object ReactorPowerApp {
  val random = new Random
  val secondsPerDay = 60 * 60 * 24
  val nanosToSecondsFactor = 1000 * 1000 * 1000L
  val numberOfHouseholds = 1000
  val host = "localhost"
  val port = 11111
  var lastTimePassedNanos = 0L

  /** A Measurement of the consumption of electricity in one household. */
  case class Measurement(modelSecond: Int, householdId: Int, consumptionMW: Double, ingresTimeRealNanos: Long) {
    def time: LocalTime = LocalTime.ofSecondOfDay(modelSecond)
  }

  /** The consumption of a specific number of households is summed up. */
  case class MeasurementSum(modelSecond: Int, consumptionMW: Double, lastIngresTimeRealNanos: Long, count: Long)

  /** Characterizes the Prediction of the needed production of electricity in megawatt for a specific number of households in the next second. */
  case class Prediction(modelSecond: Int, productionMW: Double, lastIngresTimeRealNanos: Long, latencyNanos: Long)

  /** Reads the Measurements as a Client from the Socket and predicts the Consumption for the next second.*/
  def main(args: Array[String]): Unit = {
    val socket = new Socket(host, port)
    val reader = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val readerLines = reader.lines
    val startTime = System.nanoTime()
    val csvLines = Flux.fromStream(readerLines)
    predictionProcessing(startTime, csvLines)
      .`then`().block()
  }

  /** Consumes the arriving CSV Measurements and uses them for the Prediction for the next second.*/
  private def predictionProcessing(startTimeNanos: Long, csvLines: Flux[String]): SFlux[Unit] = {
    val measurements = SFlux(csvLines)
      .map { csvLine: String =>
        val columns = csvLine.split(",")
        val modelSecond = columns(0).toInt
        val householdId = columns(1).toInt
        val consumption = columns(2).toDouble
        Measurement(modelSecond = modelSecond, householdId = householdId, consumptionMW = consumption, ingresTimeRealNanos = System.nanoTime())
      }

    /** Transforms the needed Scala function into a Java function. */
    val secondOfMeasurement: JavaFunction[Measurement, Int] = {
      (x: Measurement) => x.modelSecond
    }

    /** Needed to recognize the change in second and when to sum up all households for one second. */
    val bufferedPerSecond = measurements
      .asJava()
      .bufferUntilChanged[Int](secondOfMeasurement)

    /** Transforms it back to an SFlux, sums up the Measurements and predicts the next second for the sum of Measurements, while having a rolling aggregation window. */
    val totalConsumptionRollingHistory = SFlux(bufferedPerSecond)
      .map(x => totalConsumptionPerSecond(x))
      .buffer(60)(1)

    sequentialPrediction(totalConsumptionRollingHistory)
      .map{
        prediction =>
          //printPrediction(prediction)
          printLatencyEverySec(startTimeNanos = startTimeNanos, prediction)
      }
      .doOnError { (t: Throwable) => println(t) }
  }

  /** Parallelizes the Prediction Step. Not worth the effort, because the buffer size is too small. */
  def parallelPrediction(SFlux: SFlux[Seq[MeasurementSum]]) : SFlux[Prediction] = {
    SFlux.parallel()
      .runOn(Schedulers.parallel())
      .map(x => predictionForProduction(x))
      .sequential()
  }

  /** Prediction made in a sequential way. */
  def sequentialPrediction(SFlux: SFlux[Seq[MeasurementSum]]) : SFlux[Prediction] = {
    SFlux
      .map(x => predictionForProduction(x))
  }


  /** Sums up all Measurements of consumption measured in one second. */
  def totalConsumptionPerSecond(measurements: java.util.List[_ <: Measurement]): MeasurementSum = {
    val aggregated = measurements.asScala.foldLeft(MeasurementSum(measurements.get(0).modelSecond, 0, System.nanoTime(), 0)) {
      (accu, item) =>
        assert(accu.modelSecond == item.modelSecond)
        MeasurementSum(accu.modelSecond, accu.consumptionMW + item.consumptionMW, item.ingresTimeRealNanos, accu.count + 1)
    }
    aggregated
  }

  /** Predicts the expected consumption for the next second. Implemented by linear regression. */
  def predictionForProduction(input: Seq[MeasurementSum]): Prediction = {
    val inputArray = input.toArray
    val xs = inputArray.map(item => item.modelSecond.toDouble)
    val ys = inputArray.map(item => item.consumptionMW)

    val prediction = new LinearRegression(xs, ys).predict(inputArray.last.modelSecond + 1)

    val lastIngresTimeRealNanos = inputArray.last.lastIngresTimeRealNanos
    Prediction(inputArray.last.modelSecond + 1, prediction, lastIngresTimeRealNanos, System.nanoTime() - lastIngresTimeRealNanos)
  }

  private val samplingDivisor = 2000

  def printPrediction(item: Prediction): Unit = {
    if (item.modelSecond % samplingDivisor == 0) {
      println(s"${item.modelSecond}, ${item.productionMW}, ${item.latencyNanos}, ${System.currentTimeMillis()}")
    }
  }

  var printNumber = 0
  def printLatencyEverySec(startTimeNanos: Long, item: Prediction): Unit = {
    val nowNanos = System.nanoTime()
    val timePassedNanos = nowNanos - startTimeNanos
    val timePassedSeconds = timePassedNanos / nanosToSecondsFactor
    if (timePassedSeconds > lastTimePassedNanos / nanosToSecondsFactor) {
      println(s"${item.latencyNanos}")
      printNumber += 1
      lastTimePassedNanos = timePassedNanos
    }
  }



}