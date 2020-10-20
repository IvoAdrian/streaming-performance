import java.net.{ServerSocket, Socket}
import java.io._

import scala.util.Random

  object Generator {

    lazy val nanosToSecondsFactor = 1000 * nanosToMillisFactor
    lazy val nanosToMillisFactor = 1000 * 1000L
    val secondsPerDay = 60 * 60 * 24
    val random = new Random

    def main(args: Array[String]): Unit = {
      val throughput = args(0).toInt
      val server = new Generator
      server.generateToTCP(throughput)
    }

    def nanosToEvents(throughput: Int, timePassedNanos: Long): Long = {
      throughput * timePassedNanos / nanosToSecondsFactor
    }

    def eventsToNanos(throughput: Int, events: Long): Long = {
      events * nanosToSecondsFactor / throughput
    }

    case class SleepArgs(milliseconds: Long, nanoseconds: Int)

    def nanosToSleepArgs(delayNanos: Long): SleepArgs = {
      val delayNanosforSleep: Long = delayNanos % nanosToMillisFactor
      val delayMillis: Long = delayNanos / nanosToMillisFactor
      SleepArgs(delayMillis, delayNanosforSleep.toInt)
    }
  }

  class Generator {
    import Generator._


    var lastTimePassedNanos = 0L
    var lastNumberOfEventsWritten = 0L

    @throws[IOException]
    def generateToTCP(throughput: Int): Unit = {
      val port = 11111
      val serverSocket = new ServerSocket(port)
      val clientSocket = awaitConnection(serverSocket)
      val printWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream))
      println(s"generating measurements with throughput: $throughput")
      val startTimeNanos = System.nanoTime()
      while (true) {
        val nowNanos = System.nanoTime()
        val timePassedNanos = nowNanos - startTimeNanos
        //println(s"time passed nanos: $timePassedNanos")
        logEffectiveThroughput(startTimeNanos)
        val numberOfEventsToBeReached = nanosToEvents(throughput = throughput, timePassedNanos)
        val numberOfEventsToBeWritten = numberOfEventsToBeReached - numberOfEventsWritten
        //println(s"number of events to be written: $numberOfEventsToBeWritten")
        if (numberOfEventsToBeWritten > 0) {
          writeEvents(startTimeNanos, printWriter, numberOfEventsToBeWritten)
        } else if (numberOfEventsToBeWritten < 0) {
          val delayNanos = -eventsToNanos(throughput, numberOfEventsToBeWritten)
          val sleepArgs: SleepArgs = nanosToSleepArgs(delayNanos)
          Thread.sleep(sleepArgs.milliseconds, sleepArgs.nanoseconds)
        }
      }
    }

    val numberOfHouseholds = 1000
    val twoKilowattsInMW = 0.002
    val amplitudeMW = twoKilowattsInMW / 2
    val maximumPhaseShift = secondsPerDay / 3

    val unitCircumference = 2 * math.Pi

    val households = (0 until numberOfHouseholds).map { householdId =>
      Household(twoKilowattsInMW * random.nextDouble(), amplitudeMW * random.nextDouble(), random.nextInt(maximumPhaseShift))
    }

    /** Characterizes a household and it's consumption behaviour. */
    case class Household(averageConsumptionMW: Double, amplitudeMW: Double, phaseShiftSeconds: Int)


    /** Delivers the typical consumption in megawatt. */
    def consumption(household: Household, second: Int): Double = {
      val secondOfDay = second % secondsPerDay
      val sinArg = (secondOfDay + household.phaseShiftSeconds) * unitCircumference / secondsPerDay
      val consumption = household.amplitudeMW * math.sin(sinArg) + household.averageConsumptionMW
      consumption
    }

    def awaitConnection(serverSocket: ServerSocket): Socket = {
      serverSocket.accept
    }

    var numberOfEventsWritten = 0L

    def writeEvents(startTimeNanos: Long, printWriter: PrintWriter, numberOfEventsToBeWritten: Long): Unit = {
      var i = 0L
      while (i < numberOfEventsToBeWritten) {
        logEffectiveThroughput(startTimeNanos)
        val modelSecond = numberOfEventsWritten / numberOfHouseholds
        val householdId = numberOfEventsWritten % numberOfHouseholds
        val householdConsumption = consumption(households(householdId.toInt), modelSecond.toInt)
        printWriter.println(s"$modelSecond,$householdId,$householdConsumption")
        numberOfEventsWritten += 1
        i += 1
      }
    }

    def logEffectiveThroughput(startTimeNanos: Long): Unit = {
      val nowNanos = System.nanoTime()
      val timePassedNanos = nowNanos - startTimeNanos
      val timePassedSeconds = timePassedNanos / nanosToSecondsFactor
      if (timePassedSeconds > lastTimePassedNanos / nanosToSecondsFactor) {
        val intervalDurationRealNanos = timePassedNanos - lastTimePassedNanos
        val numberOfMeasurementsInInterval = numberOfEventsWritten - lastNumberOfEventsWritten
        val effectiveThroughput = numberOfMeasurementsInInterval * nanosToSecondsFactor / intervalDurationRealNanos
        println(s"effective throughput: $effectiveThroughput")
        lastTimePassedNanos = timePassedNanos
        lastNumberOfEventsWritten = numberOfEventsWritten
      }
    }

  }

