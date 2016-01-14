import java.text.SimpleDateFormat
import java.util.Calendar

import ResponseTime.getResponseTime
import RetweetCount.updateRetweetCount
import com.datastax.spark.connector.SomeColumns
import com.datastax.spark.connector.streaming._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.joda.time.DateTime
import utils.SentimentAnalysisUtils._

class Streamer extends Serializable {

  /**
   * Filters with keywords, extract tweet properties, save to Cassandra and start StreamingContext
   * @param ssc
   * @param keyspace
   * @param table
   */
  def start(ssc: StreamingContext, keyspace: String, table: String) {

    // Select some words, accounts or hashtags to stream
    val filters = Seq("orange", "orange_france", "sosh", "sosh_fr", "orange_conseil")

    // Filter out tweets containing off topic words or hashtags
    val offTopicWords = Seq("jus", "juice", "alert", "alerte", "OINTB", "#OINTB", "black", "vigilance", "canard")
    val offTopicHashtags = Seq("#alert", "#alerte", "#vigilance", "#OINTB", "OrangeIsTheNewBlack")
    val offTopicFilters = offTopicWords ++ offTopicHashtags

    val stream = TwitterUtils.createStream(ssc, None, filters).filter(_.getLang == "fr")

    val timestampFormatBySecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val timestampFormatByMinute = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    // Stream tweets
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
          t.getURLEntities.map(_.getExpandedURL).mkString(",").split(",").toList,
          detectSentiment(t.getText).toString
        )
      }

    tweet.print()
    tweet.saveToCassandra(keyspace, table, SomeColumns("body", "user_id", "user_screen_name", "lang", "created_at", "favorite_count", "retweet_count", "tweet_id", "reply_id", "response_time", "user_mentions", "hashtags", "urls", "sentiment"))

    // Update number of retweets of already streamed tweets
    val rt = stream
      .filter(_.isRetweet == true)
      .filter(_.getText.split(" ").exists(offTopicFilters contains _) == false)
      .map { t =>
        updateRetweetCount(TwitterStreamingApp.conf, t.getRetweetedStatus.getRetweetCount, t.getRetweetedStatus.getId, t.getRetweetedStatus.getUser.getId)
      }

    // Count the number of tweets in a 60 seconds window
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

    // Get top 10 trending topics through hashtags
    val hashtags = stream
      .filter(_.getText.split(" ").exists(offTopicFilters contains _) == false)
      .flatMap(t => t.getText.split(" ")).filter(_.startsWith("#"))

    val trending60 = hashtags
      .map((_, 1))
      .reduceByKeyAndWindow(_ + _, Seconds(60))
      .map { case (hashtag, count) => (
          timestampFormatByMinute.format(Calendar.getInstance().getTime()),
          Map[String, Int](hashtag -> count)
        )
      }

    trending60.saveToCassandra("twitter_streaming", "trends", SomeColumns("date", "hashtags"))

    ssc.checkpoint("./checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }
}
