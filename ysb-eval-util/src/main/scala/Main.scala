import com.redis._
import java.io._
import scala.math.max

object Main {

  case class CampaignWindowCondensed ( time_window_start: Long, seen_count: Int,  time_updated: Long)

  case class TimeWindowCondensed(seen_count: Int, time_updated: Long)

  case class TimeWindowCountAndLatency(time_window_start: Long, seen_count: Int, last_updated: Long, latency: Long)

  case class AllTimeWindowsStablePhase(seen_count_sum_10s: Int, stable_latency: Long)

  // check for congruency in the original benchmark
  val windowDurationMs: Long = 1000

  def main(args: Array[String]): Unit = {
    val r = new RedisClient("localhost", 6379)
    val keysOption: Option[List[Option[String]]] = r.keys("*")
    val list = keysOption.get

    val ids = for {
      stringOption <- list
      id <- stringOption
    } yield id

    val redisList = for {
      id <- ids
      if r.getType(id).contains("hash")
    } yield id -> r.hmget(id, "seen_count", "time_updated", "time_window_start").get
    val filteredRedisList = redisList.filter(x => x._2.nonEmpty)
    val campaignWindowCondensedList = filteredRedisList.map{
      x => CampaignWindowCondensed(x._2("time_window_start").toLong, x._2("seen_count").toInt, x._2("time_updated").toLong)
    }
    val grouped = campaignWindowCondensedList.groupBy(x => x.time_window_start)
    val folded = grouped.mapValues{
      list => list.foldLeft(TimeWindowCondensed(0, 0)) {
        (accu: TimeWindowCondensed, x: CampaignWindowCondensed) =>
          TimeWindowCondensed(accu.seen_count + x.seen_count, max(accu.time_updated, x.time_updated))
      }
    }
    val sorted = folded.toList.sortBy(x => x._1)
    val countAndLatency = sorted.map(x => TimeWindowCountAndLatency(x._1, x._2.seen_count, x._2.time_updated, x._2.time_updated - x._1 - windowDurationMs))

    val listCountAndLatency = countAndLatency.map(x => s"${x.latency}")
    println(listCountAndLatency.mkString("\n"))

  }

}
