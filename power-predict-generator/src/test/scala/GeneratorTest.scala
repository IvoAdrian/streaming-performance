import java.time.temporal.ChronoUnit

import org.scalatest.funsuite.AnyFunSuite
import Generator.{SleepArgs, eventsToNanos, nanosToEvents, nanosToSleepArgs}

import scala.concurrent.duration.Duration


class GeneratorTest extends AnyFunSuite {

  test("Nanoseconds to Events") {
    assert(nanosToEvents(1000, 1000000000) == 1000)
  }

  test("Events to Nanoseconds") {
    assert(eventsToNanos(1000, 1000) == 1000000000)
    assert(eventsToNanos(throughput = 1000, events = -1000) == -1000000000)
  }

  test("Nanos to Sleep Arguments") {
    assert(nanosToSleepArgs(23100000L) == SleepArgs(23L, 100000))
    assert(nanosToSleepArgs(999999L) == SleepArgs(0L, 999999))
    assert(nanosToSleepArgs(123456789123456789L) == SleepArgs(123456789123L, 456789))
  }
}