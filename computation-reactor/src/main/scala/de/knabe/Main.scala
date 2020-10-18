package de.knabe

import reactor.core.scala.publisher.{SFlux, SParallelFlux}
import reactor.core.scheduler.Schedulers

import scala.math.sqrt

object Main {

  val workloadAmount = 10000

  def main(args: Array[String]): Unit = {
    val random = scala.util.Random
    case class Item(seqNr: Long, core: Int, creationTimeNanos: Long)

    val parallelism = 4
    val eventCount = 200000000

    val flux = SFlux.range(1, eventCount)
      .map(x => Item(x.toLong, random.nextInt(parallelism), System.nanoTime()))

    def workload(x: Item): Item =  {
      var i = 0L
      while (i < 100000L) {
        i = i + 1;
      }
      x
    }

    def processing(x: Item): Item = {
      val sqrtD = sqrt(x.seqNr.toDouble)
      val rounded = (sqrtD * sqrtD).round
      Item(rounded, x.core, x.creationTimeNanos)
    }

    val startNanos = System.nanoTime();

    def parallel(sflux: SParallelFlux[Item]): SParallelFlux[Item] = {
      sflux
        .filter{x => x.seqNr % 2 == 0}
        .map(x => x.copy(seqNr = x.seqNr / 2))
        .map(x => processing(x))
        .map(x => workload(x))
        .map(x => {
          if (x.seqNr % 10000 == 0) {
            val currentTimeNanos = System.nanoTime()
            val runDurationNanos = currentTimeNanos - startNanos
            val latencyNanos = currentTimeNanos - x.creationTimeNanos
            println(s"$runDurationNanos,$latencyNanos")
          }; x
        })
    }

    val parallelFlux = flux
      .parallel().runOn(Schedulers.parallel())

    val parallelRun = parallel(parallelFlux)
      .sequential()
      .fold(0L)((accu, value)
      => accu + value.seqNr)

    parallelRun
      //.map(i => println(s"parallel: result = $i"))
      .`then`()
      .block()

  }
}
