import java.lang.System.currentTimeMillis
import java.util.Collections
import java.util
import java.util.UUID.randomUUID
import java.util.function.{Function => JavaFunction}

import scala.jdk.CollectionConverters._
import scala.concurrent.{ExecutionContextExecutor, Future}

import reactor.core.scala.publisher.{SFlux, SMono}
import reactor.kafka.receiver.{KafkaReceiver, ReceiverOptions}

import org.apache.kafka.clients.consumer.{ConsumerConfig}
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.StringDeserializer

import akka.actor.ActorSystem
import akka.util.ByteString

import spray.json._
import redis.RedisClient


object Main {

  case class AdEvent(user_id: String, page_id: String, ad_id: String, ad_type: String, event_type: String, event_time: String, ip_address: String)

  case class AdEventProjected(ad_id: String, event_time: String)

  case class AdEventWithCampaign(campaign_id: String, ad_id: String, event_time: String)

  case class CampaignWindow2Ad(key: CampaignWindow, ad_id: String)

  case class CampaignWindow(campaign_id: String, window_time: Long)

  case class CampaignAndWindowChange(key: CampaignWindow, window_changed: Boolean)

  type Campaign2CountMap = Map[String, Int]

  case class WindowTimeCampaignCounts(window_time: Long, campaign_2_count_map: Campaign2CountMap)

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val adEventFormat: RootJsonFormat[AdEvent] = jsonFormat7(AdEvent)
  }

  val windowDurationMs: Long = 1000

  def main(args: Array[String]): Unit = {
    import MyJsonProtocol._

    val system: ActorSystem = ActorSystem("reactor-benchmark")
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    val redis = RedisClient()(system)

    /** Returns a Future with the campaign id for the ad event. */
    def queryRedis(ad_event_projected: AdEventProjected) = {
      //log.debug(ad_event_projected.toString);
      redis.get(ad_event_projected.ad_id)
        .map { optionByteStringCampaignId => optionByteStringCampaignId.getOrElse(ByteString.fromString("campaign_id for ad_id not found")).utf8String }
        .map(campaignId => AdEventWithCampaign(campaignId, ad_event_projected.ad_id, ad_event_projected.event_time))
    }

    def writeRedisTopLevel(campaignWindowTimeCount: WindowTimeCampaignCounts, redis: RedisClient): Future[String] = {
      val futures = campaignWindowTimeCount.campaign_2_count_map.map{
        case (campaign: String, count: Int) => writeWindow(redis, campaign, campaignWindowTimeCount.window_time.toString, count)
      }
      Future.sequence(futures).map(x => campaignWindowTimeCount.window_time + s" window aggregated")
    }

    /** Returns a Future with the UUID of the campaign window. */
    def writeWindow(redis: RedisClient, campaign: String, window_timestamp: String, window_seenCount: Int): Future[String] = {
      for {
        optionValueWindowUUID <- redis.hget(campaign, window_timestamp)
        //_ = log.debug("{},{} -> {} found in REDIS1", campaign, window_timestamp, optionValueWindowUUID)
        windowUUID: String <- {
          optionValueWindowUUID match {
            case Some(windowUUIDFromRedis) =>
              val windowUUID = windowUUIDFromRedis.utf8String
              //log.debug("{},{} -> {} YES found in REDIS", campaign, window_timestamp, windowUUID)
              Future(windowUUID)
            case None =>
              //log.debug("{},{} -> Not found in REDIS", campaign, window_timestamp)
              val windowUUID = randomUUID().toString

              for {
                ok <- redis.hset(campaign, window_timestamp, windowUUID)
                //_ = log.debug("written new windowUUID {} to Redis: {}", windowUUID, ok)
                seqOptionValueWindowListUUID <- redis.hget(campaign, "windows")
                windowListUUID <- seqOptionValueWindowListUUID match {
                  case None =>
                    val windowListUUID = randomUUID().toString
                    redis.hset(campaign, "windows", windowListUUID).map(_ => windowListUUID)
                  case Some(windowListUUIDFromRedis) => Future(windowListUUIDFromRedis.utf8String)
                }
                _ <- redis.lpush(windowListUUID, window_timestamp)
              } yield windowUUID
          }
        }
        _ <- redis.hincrby(windowUUID, "seen_count", window_seenCount)
        _ <- redis.hset(windowUUID, "time_updated", currentTimeMillis().toString)
        _ <- redis.hset(windowUUID, "time_window_start", window_timestamp)
      } yield window_seenCount.toString
    }

    val consumerProps = new util.HashMap[String, AnyRef]
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group")
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[IntegerDeserializer])
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])

    val receiverOptions =
      ReceiverOptions.create[Integer, String](consumerProps).subscription(Collections.singleton("ad-events"))

    val kafkaReceiver =
      SFlux.apply(KafkaReceiver.create(receiverOptions).receive())

    val kafkaRawData =
      kafkaReceiver.map((message) => message.value)

    val kafkaData =
      kafkaRawData.map((json_string: String) => json_string.parseJson.convertTo[AdEvent])

    val filteredOnView =
      kafkaData.filter(ad_event => ad_event.event_type == "view")

    val projected =
      filteredOnView.map { ad_event_filtered => AdEventProjected(ad_event_filtered.ad_id, ad_event_filtered.event_time) }

    val redisJoined =
      projected.flatMap(x => SMono.fromFuture(queryRedis(x)))

    val campaign_timeStamp =
      redisJoined.map(campaignTime)

    /** Transforms the needed Scala function into a Java function. */
    val windowTime: JavaFunction[CampaignWindow2Ad, Long] = {
      (x: CampaignWindow2Ad) => x.key.window_time
    }

    val timeWindowBufferedCampaignWindow2Ads =
      SFlux(campaign_timeStamp.asJava().bufferUntilChanged[Long](windowTime)).map(x => x.asScala)

    val totalEventsPerCampaignTime: SFlux[WindowTimeCampaignCounts] =
      timeWindowBufferedCampaignWindow2Ads
        .map { (buffer) =>
          buffer.foldLeft(WindowTimeCampaignCounts(window_time = 0L, campaign_2_count_map = Map(): Campaign2CountMap))((accu, campaignWindow2Ad) =>
            WindowTimeCampaignCounts(campaignWindow2Ad.key.window_time, count(accu.campaign_2_count_map, campaignWindow2Ad.key.campaign_id)))
        }

    val writtenBackToRedis =
      totalEventsPerCampaignTime.flatMap(x => SMono.fromFuture(writeRedisTopLevel(x, redis)))


    writtenBackToRedis.subscribe(
      r => {
        System.out.printf("Received message: %s\n", r);
      },
      Some(t => println(t))
    )

  }
  def campaignTime(event: AdEventWithCampaign): CampaignWindow2Ad = {
    val eventTime = event.event_time.toLong
    val windowTime = windowDurationMs * (eventTime / windowDurationMs)
    CampaignWindow2Ad(CampaignWindow(event.campaign_id, windowTime), event.ad_id)
  }

  def count(map: Campaign2CountMap, campaign_id: String): Campaign2CountMap = {
    val x = map.getOrElse(campaign_id, 0)
    map.updated(campaign_id, x + 1)
  }

}
