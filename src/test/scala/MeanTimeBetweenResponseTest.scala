import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkConf
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest._

class MeanTimeBetweenResponseTest extends FlatSpec with BeforeAndAfter with GivenWhenThen with Matchers {
  private val master = "local[2]"
  private val appName = "spark-streaming-twitter"

  val conf = new SparkConf()
    .setMaster(master)
    .setAppName(appName)
    .set("spark.cassandra.connection.host", "localhost")

  before {
    CassandraConnector(conf).withSessionDo { session =>
      session.execute("CREATE KEYSPACE IF NOT EXISTS twitter_streaming WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1}")
      session.execute(
        """
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
      session.execute("CREATE INDEX IF NOT EXISTS ON twitter_streaming.tweets(tweet_id);")

      // Insert a mock question tweet for Sosh_fr
      session.execute("INSERT INTO twitter_streaming.tweets (body, user_id, user_screen_name, lang, created_at, favorite_count, retweet_count, tweet_id, user_mentions, reply_id, mean_time, hashtags, urls) VALUES('@Sosh_fr Ceci est un test', 63721, 'user', 'fr', '2015-11-30 18:23:49+0100', 0, 0, 3258473, ['Sosh_fr'], null, null, [''], [''])")
    }
  }

  after {
    CassandraConnector(conf).withSessionDo { session =>
      session.execute("DROP KEYSPACE twitter_streaming;")
    }
  }

  "Response time" should "be counted" in {
    Given("a tweet response")
    val meanTime = MeanTimeBetweenResponse.getTweets(conf, 3258473, new DateTime(2015, 11, 30, 18, 28, 12, DateTimeZone.forID("Europe/Paris")))
    When("a corresponding question is found")

    Then("the date difference should match")
    println(meanTime)
    meanTime should be ("00:04:23")
  }
}
