import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkContext, SparkConf}

/**
 * Set Twitter credentials in src/main/resources/twitter_credentials.txt
 *
 * or export as environment variables: !!! TO DO !!!
 *    export TWITTER_CONSUMER_KEY="value"
 *    export TWITTER_CONSUMER_SECRET="value"
 *    export TWITTER_ACCESS_TOKEN="value"
 *    export TWITTER_ACCESS_TOKEN_SECRET="value"
 *
 * or pass them as -D system properties:
 *    -Dtwitter4j.oauth.consumerKey="value"
 *    -Dtwitter4j.oauth.consumerSecret="value"
 *    -Dtwitter4j.oauth.accessToken="value"
 *    -Dtwitter4j.oauth.accessTokenSecret="value"
 *
 * Tweets are saved to a Cassandra instance. To verify persisted data with cqlsh:
 * cqlsh> SELECT * FROM twitter_streaming.tweets
 *
 */

object TwitterStreamingApp {

  // Set Twitter credentials
  TwitterSettings.configureTwitterCredentials()

  // Set Spark configuration and context
  val conf = new SparkConf()
    .setMaster("local[2]")
    .setAppName("TwitterStreamingApp")
    .set("spark.cassandra.connection.host", "localhost")
  val sc = new SparkContext(conf)
  val ssc = new StreamingContext(sc, Seconds(5))

  def main(args: Array[String]): Unit = {
    setUpCassandra()

    val stream = new Streamer
    stream.start(ssc, "twitter_streaming", "tweets")
  }

  /**
   * Creates the keyspace and table schema for tweets
   */
  def setUpCassandra(): Unit = {
    CassandraConnector(conf).withSessionDo { session =>
      session.execute("CREATE KEYSPACE IF NOT EXISTS twitter_streaming WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1}")
      session.execute("""
        CREATE TABLE IF NOT EXISTS twitter_streaming.tweets (
          body text,
          user_id bigint,
          user_screen_name text,
          lang text,
          created_at timestamp,
          favorite_count int,
          retweet_count int,
          tweet_id bigint,
          user_mentions list<text>,
          reply_id bigint,
          mean_time text,
          hashtags list<text>,
          urls list<text>,
          PRIMARY KEY (body, tweet_id, user_id, user_screen_name)
        )"""
      )
    }
  }
}
