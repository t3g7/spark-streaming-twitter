import java.sql.Timestamp
import java.util.Date

import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.streaming._
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ConstantInputDStream

/**
  * Created by maxime on 23/11/15.
  */

object MeanTimeBetweenResponse{

//  def getTweets(inReplyToStatusId : Long, timestamp : String ){

//      CassandraConnector(TwitterStreamingApp.conf).withSessionDo { session =>
//        val it = Iterator (session.execute("SELECT reply_id FROM twitter_streaming.tweets WHERE reply_id > -1 ALLOW FILTERING"))
//        val reply_id = session.execute("SELECT reply_id FROM twitter_streaming.tweets WHERE reply_id > -1 ALLOW FILTERING")
//        while(it.hasNext){
//          it.next()
//          print(reply_id.one().toString)
//        }

  //      print("test")

//  }
  def getTweets(ssc: StreamingContext, keyspace: String, table: String, inReplyToStatusId: Long, createdAt: String) {
    val cassandraRDD = ssc.cassandraTable(keyspace, table).select("created_at").where("tweet_id = ?", inReplyToStatusId)
    val dstream = new ConstantInputDStream(ssc, cassandraRDD)

    dstream.foreachRDD { rdd =>
      println(rdd.collect.mkString("\n"))
    }
  }

}
