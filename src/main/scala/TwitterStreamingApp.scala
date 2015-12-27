import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import utils._

/**
 * Set Twitter credentials in src/main/resources/twitter_credentials.txt
 *
 * or pass them as arguments, see usage
 *
 * Tweets are saved to a Cassandra instance. To verify persisted data with cqlsh:
 * cqlsh> SELECT * FROM twitter_streaming.tweets
 *
 */

object TwitterStreamingApp {

  // Set Spark configuration and context
  val conf = new SparkConf()
    .setMaster("local[2]")
    .setAppName("TwitterStreamingApp")
    .set("spark.cassandra.connection.host", "localhost")
  val sc = new SparkContext(conf)
  val ssc = new StreamingContext(sc, Seconds(5))

  def main(args: Array[String]): Unit = {
    CassandraSettings.setUp(conf)

    // Set Twitter credentials
    if (args.length == 0) {
      TwitterSettings.setTwitterCredentialsFromFile()
    } else if (args.length < 4) {
      System.err.println("Usage: TwitterStreamingApp <consumer key> <consumer secret> <access token> <access token secret> \n       Or set credentials in text file")
      System.exit(1)
    } else {
      val Array(consumerKey, consumerSecret, accessToken, accessTokenSecret) = args.take(4)
      TwitterSettings.setTwitterCredentialsFromArgs(consumerKey, consumerSecret, accessToken, accessTokenSecret)
    }

    val stream = new Streamer
    stream.start(ssc, "twitter_streaming", "tweets");
  }
}
