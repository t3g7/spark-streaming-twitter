import java.text.SimpleDateFormat
import org.joda.time.DateTime

import com.datastax.spark.connector.streaming._
import com.datastax.spark.connector.SomeColumns
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.twitter.TwitterUtils
import ResponseTime.getResponseTime


class Streamer extends Serializable {

  /**
   * Filters with keywords, extract tweet properties, save to cassandra and start StreamingContext
   * @param ssc
   * @param keyspace
   * @param table
   */
  def start(ssc: StreamingContext, keyspace: String, table: String) {

    // Select some words, accounts or hashtags to stream
    val filters = Seq("orange", "orange_france", "sosh", "sosh_fr", "orange_conseil")

    // Filter out tweets containing off topic words or hashtags
    val offTopicWords = Seq("jus", "juice", "alert", "alerte", "OINTB", "#OINTB", "black", "vigilance")
    val offTopicHashtags = Seq("#alert", "#alerte", "#vigilance", "#OINTB", "OrangeIsTheNewBlack")
    val offTopicFilters = offTopicWords ++ offTopicHashtags

    val stream = TwitterUtils.createStream(ssc, None, filters).filter(_.getLang == "fr")

    val timestampFormatBySecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val timestampFormatByMinute = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    val tweet = stream
      .filter(_.isRetweet == false)
      .filter(_.getText.split(" ").exists(offTopicFilters contains _) == false)
      .map { t => (
          t.getText,
          t.getUser.getId,
          t.getUser.getScreenName,
          t.getLang,
          timestampFormatBySecond.format(t.getCreatedAt),
          t.getFavoriteCount,
          t.getRetweetCount,
          t.getId,
          t.getInReplyToStatusId,
          getResponseTime(TwitterStreamingApp.conf, t.getInReplyToStatusId, new DateTime(t.getCreatedAt)),
          t.getUserMentionEntities.map(_.getScreenName).mkString(",").split(",").toList,
          t.getHashtagEntities.map(_.getText).mkString(",").split(",").toList,
          t.getURLEntities.map(_.getExpandedURL).mkString(",").split(",").toList
        )
      }

    tweet.print()
    tweet.saveToCassandra(keyspace, table, SomeColumns("body", "user_id", "user_screen_name", "lang", "created_at", "favorite_count", "retweet_count", "tweet_id", "reply_id", "response_time", "user_mentions", "hashtags", "urls"))

    val count = 0
    val freq = stream
      .filter(_.isRetweet == false)
      .filter(_.getText.split(" ").exists(offTopicFilters contains _) == false)
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
