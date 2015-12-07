import java.text.SimpleDateFormat
import org.joda.time.DateTime

import com.datastax.spark.connector.streaming._
import com.datastax.spark.connector.SomeColumns
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.twitter.TwitterUtils
import MeanTimeBetweenResponse.getTweets


class Streamer extends Serializable{

  /**
   * Filters with keywords, extract tweet properties, save to cassandra and start StreamingContext
   * @param ssc
   * @param keyspace
   * @param table
   */
  def start(ssc: StreamingContext, keyspace: String, table: String) {

    val filters = Seq("orange", "orange_france", "sosh", "sosh_fr", "orange_conseil")
    val stream = TwitterUtils.createStream(ssc, None, filters).filter(_.getLang == "fr")

    val timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    val tweet = stream
      .filter(_.isRetweet == false)
      .map { t => (
          t.getText,
          t.getUser.getId,
          t.getUser.getScreenName,
          t.getLang,
          timestampFormat.format(t.getCreatedAt),
          t.getFavoriteCount,
          t.getRetweetCount,
          t.getId,
          t.getInReplyToStatusId,
          getTweets(TwitterStreamingApp.conf, t.getInReplyToStatusId,new DateTime(t.getCreatedAt)),
          t.getUserMentionEntities.map(_.getScreenName).mkString(",").split(",").toList,
          t.getHashtagEntities.map(_.getText).mkString(",").split(",").toList,
          t.getURLEntities.map(_.getExpandedURL).mkString(",").split(",").toList
        )
      }

    tweet.print()
    tweet.saveToCassandra(keyspace, table, SomeColumns("body", "user_id", "user_screen_name", "lang", "created_at", "favorite_count", "retweet_count", "tweet_id", "reply_id", "mean_time", "user_mentions", "hashtags", "urls"))
    ssc.checkpoint("./checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }
}
