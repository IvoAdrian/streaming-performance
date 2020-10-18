package de.knabe

import java.time.LocalTime

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString

import scala.concurrent.{Await, duration}
import scala.concurrent.duration.Duration
import scala.util.Random


object Main {
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

  def main(args: Array[String]): Unit = {
    val system: ActorSystem = ActorSystem("akka-benchmark")
    implicit val materializer: Materializer = ActorMaterializer()(system)

    val connection = Tcp(system).outgoingConnection("localhost", 11111)
    val startTime = System.nanoTime()

    val socketFlow = Flow[ByteString]
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
      //.log("each", _.utf8String)
      .map(_.utf8String)
      .via(consumingFlux(startTime))
      .fold(ByteString.empty)((accu, _) => accu)

    Await.result(connection.join(socketFlow).run(), atMost = Duration(200, duration.MINUTES))
  }

  val linesToUnits = Flow[String].map(line => println("Measurement2: " + line))

  def consumingFlux(startTime: Long): Flow[String, Any, NotUsed] = {
    val measurements = Flow[String]
      .map { line: String =>
        val columns = line.split(",")
        val modelSecond = columns(0).toInt
        val householdId = columns(1).toInt
        val consumption = columns(2).toDouble
        Measurement(modelSecond = modelSecond, householdId = householdId, consumptionMW = consumption, ingresTimeRealNanos = System.nanoTime())
      }
    measurements
      .sliding(2, 1)
      .splitWhen(x => x(0).modelSecond != x(1).modelSecond)
      .map(x => x(1))
      //.map{x => println(x); x}
      .fold(List[Measurement]())((accu, item) => item::accu)
      .map(x => totalConsumptionPerSecond(x))
      .mergeSubstreams
      .sliding(60, 1)
      .map(x => predictionForProduction(x))
      .map(printPredictionEverySec(startTimeNanos = startTime, _))
  }

  /** Sums up all Measurements of consumption measured in one second. */
  def totalConsumptionPerSecond(measurements: Seq[Measurement]): MeasurementSum = {
    val aggregated = measurements.foldLeft(MeasurementSum(measurements(0).modelSecond, 0, System.nanoTime(), 0)) {
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

  var printNumber = 0
  def printPredictionEverySec(startTimeNanos: Long, item: Prediction): Unit = {
    val nowNanos = System.nanoTime()
    val timePassedNanos = nowNanos - startTimeNanos
    val timePassedSeconds = timePassedNanos / nanosToSecondsFactor
    if (timePassedSeconds > lastTimePassedNanos / nanosToSecondsFactor) {
      println(s"${item.latencyNanos}")
      printNumber += 1
      lastTimePassedNanos = timePassedNanos
    }
  }

  private val samplingDivisor = 2000

  def printPrediction(item: Prediction): Unit = {
    if (item.modelSecond % samplingDivisor == 0) {
      println(s"${item.modelSecond}, ${item.productionMW}, ${item.latencyNanos}, ${System.currentTimeMillis()}")
    }
  }

}