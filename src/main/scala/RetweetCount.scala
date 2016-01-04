import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkConf
import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.{Period, DateTime}

/**
  * Created by maxime on 04/01/16.
  */
object RetweetCount {

  /**
    * Compute the time difference between a stored tweet and its streamed response
    * @param conf
    * @param retweetCount
    * @param tweetId
    * @return a time difference
    */
  def getRetweetCount(conf : SparkConf, retweetCount : Int, tweetId : Long, userId : Long ) {
      CassandraConnector(conf).withSessionDo { session =>
        val rep = session.execute("SELECT * FROM twitter_streaming.tweets WHERE tweet_id="+ tweetId.toString +" ALLOW FILTERING")
        if(!rep.all.isEmpty)
          session.execute("UPDATE twitter_streaming.tweets SET retweet_count = " + retweetCount.toString + " WHERE user_id =" + userId.toString + " AND tweet_id=" + tweetId.toString)
      }
    }
}
