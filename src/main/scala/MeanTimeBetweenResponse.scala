import org.joda.time.{Hours, DateTime}
import com.datastax.spark.connector.cql.CassandraConnector

//
///**
//  * Created by maxime on 23/11/15.
//  */
//
object MeanTimeBetweenResponse{
//
  def getTweets(inReplyToStatusId : Long, timestamp : DateTime ): String = {

      if(inReplyToStatusId > 0){
        CassandraConnector(TwitterStreamingApp.conf).withSessionDo { session =>
          val reply_id = session.execute("SELECT created_at FROM twitter_streaming.tweets WHERE reply_id ="+ inReplyToStatusId+"ALLOW FILTERING")
          val reply_time_it = reply_id.all().get(0).getString("created_at")
//          var reply_time = "cc"
//          while(reply_time_it.){
//            print("toto")
//            reply_time = reply_time_it.next().getString("created_at")
//            print(reply_time)
//          }
//          print(reply_id.all().get(0).getString("created_at"))
          print(reply_time_it)
          val t2: org.joda.time.DateTime = DateTime.parse(reply_time_it.toString)
          val diff = Hours.hoursBetween(timestamp,t2)
          return diff.toString
          }
        }
      else
        null
  }
//  def getTweets(ssc: StreamingContext, keyspace: String, table: String, inReplyToStatusId: Long, createdAt: String) {
//    val cassandraRDD = ssc.cassandraTable(keyspace, table).select("created_at").where("tweet_id = ?", inReplyToStatusId)
//    val dstream = new ConstantInputDStream(ssc, cassandraRDD)
//
//    dstream.foreachRDD { rdd =>
//      println(rdd.collect.mkString("\n"))
//    }
//  }
//
}
