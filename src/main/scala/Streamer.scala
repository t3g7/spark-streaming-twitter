import java.text.SimpleDateFormat

import com.datastax.spark.connector.streaming._
import com.datastax.spark.connector.SomeColumns
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.twitter.TwitterUtils

class Streamer {

  /**
   * Filters with keywords, extract tweet properties, save to cassandra and start StreamingContext
   * @param ssc
   */
  def start(ssc: StreamingContext) {

    val filters = Seq("orange", "orange_france", "sosh", "sosh_fr", "orange_conseil")
    val stream = TwitterUtils.createStream(ssc, None, filters).filter(_.getLang == "fr")

    val timestampFormatBySecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val timestampFormatByMinute = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    val tweet = stream
      .filter(_.isRetweet == false)
      .map { t => (
          t.getText,
          t.getUser.getId,
          t.getUser.getScreenName,
          t.getLang,
          timestampFormatBySecond.format(t.getCreatedAt),
          t.getFavoriteCount,
          t.getRetweetCount,
          t.getId,
          t.getUserMentionEntities.map(_.getScreenName).mkString(",").split(",").toList,
          t.getHashtagEntities.map(_.getText).mkString(",").split(",").toList,
          t.getURLEntities.map(_.getExpandedURL).mkString(",").split(",").toList
        )
      }

    tweet.print()
    tweet.saveToCassandra("twitter_streaming", "tweets", SomeColumns("body", "user_id", "user_screen_name", "lang", "created_at", "favorite_count", "retweet_count", "tweet_id", "user_mentions", "hashtags", "urls"))

    val count = 0
    val freq = stream
      .filter(_.isRetweet == false)
      .map { t => (
        timestampFormatByMinute.format(t.getCreatedAt),
        count + 1
        )
      }

    freq.saveToCassandra("twitter_streaming", "freq", SomeColumns("date", "count"))

    ssc.checkpoint("./checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }
}
