import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkConf

object RetweetCount {

  /**
    * Update the retweet count of a stored tweet
    * @param conf
    * @param retweetCount
    * @param tweetId
    * @param userId
    * @return
    */
  def updateRetweetCount(conf: SparkConf, retweetCount: Int, tweetId: Long, userId: Long) {
      CassandraConnector(conf).withSessionDo { session =>
        val rep = session.execute("SELECT * FROM twitter_streaming.tweets WHERE tweet_id="+ tweetId.toString +" ALLOW FILTERING")
        if(!rep.all.isEmpty)
          session.execute("UPDATE twitter_streaming.tweets SET retweet_count = " + retweetCount.toString + " WHERE user_id =" + userId.toString + " AND tweet_id=" + tweetId.toString)
      }
    }
}
