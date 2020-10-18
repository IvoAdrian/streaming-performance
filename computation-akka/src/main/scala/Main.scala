
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer, _}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.math._

object Main extends App {
  
  implicit val system = ActorSystem("CpuBoundParallel")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val random = scala.util.Random
  case class Item(seqNr: Long, core: Int, creationTimeNanos: Long){
    def seconds: Long = creationTimeNanos/1000000000L
  }

  val parallelism = 4
  val source: Source[Item, NotUsed] = Source(1L to 200000000L).map((x: Long) => Item(x, random.nextInt(parallelism), System.nanoTime()))

  private def workload(x: Item): Item =  {
    var i = 0L
    while (i < 100000L) {
      i = i + 1;
    }
    x
  }

  private def processing(x: Item): Item = {
    val sqrtD = sqrt(x.seqNr.toDouble)
    val rounded = (sqrtD * sqrtD).round
    Item(rounded, x.core, x.creationTimeNanos)
  }

  val sequential: Flow[Item, Long, NotUsed] =
    Flow.apply[Item]
      .filter{x => x.seqNr % 2 == 0}
      .map(x => x.copy(seqNr = x.seqNr / 2))
      .map(x => processing(x))
      .map(x => workload(x))
      .mapAsync(4)((x: Item) => Future{if (x.seqNr % 10000 == 0)  {
        val currentTimeNanos = System.nanoTime()
        val runDurationNanos = currentTimeNanos - startNanos
        val latencyNanos = currentTimeNanos - x.creationTimeNanos
        println(s"$runDurationNanos,$latencyNanos")
      }; x})
      .fold(0L)((accu, value)
      => accu + value.seqNr)

  private def parallel(parallelism: Int): Flow[Item, Long, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    val partitioner = builder.add(Partition[Item](parallelism, (x:Item) => (x.core)))
    val merger = builder.add(Merge[Long](parallelism))

    for (i <- 0 until parallelism) {
      partitioner ~> sequential.async ~> merger
    }

    FlowShape(partitioner.in, merger.out)
  })

  private def parallelRun() = {
    val done = source
      .via(parallel(parallelism))
      .fold(0L)((accu, value) => accu + value)
      .runForeach(i => println(s"result = $i"))  // to test if the computation is correct
    Await.result(done, 1000000.millisecond)
  }

  val startNanos = System.nanoTime();
  {
    parallelRun()
  }

  val terminated = system.terminate()
  Await.result(terminated, 1000000.millisecond)


}
