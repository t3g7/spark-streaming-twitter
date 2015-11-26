import com.datastax.spark.connector.cql.CassandraConnector

/**
  * Created by maxime on 23/11/15.
  */

object MeanTimeBetweenResponse {

  def getTweets(){

      CassandraConnector(TwitterStreamingApp.conf).withSessionDo { session =>
        val it = Iterator (session.execute("SELECT reply_id FROM twitter_streaming.tweets WHERE reply_id > -1 ALLOW FILTERING"))
//        val reply_id = session.execute("SELECT reply_id FROM twitter_streaming.tweets WHERE reply_id > -1 ALLOW FILTERING")
        while(it.hasNext){
          it.next()
          print(it.next().one().toString)
        }
      }

  }

}
