import java.sql.Timestamp

import org.joda.time.format.{PeriodFormatterBuilder, DateTimeFormatter, DateTimeFormat}
import org.joda.time.{Period, Hours, DateTime, Duration}
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
          val reply_id = session.execute("SELECT created_at FROM twitter_streaming.tweets WHERE tweet_id="+ inReplyToStatusId+"ALLOW FILTERING")
          try {
            val reply_time_it = Option(reply_id.all().get(0).getTimestamp("created_at")).getOrElse()
            val t2 = new DateTime(reply_time_it)
            val diff = new Period(t2,timestamp)
            val hms = new PeriodFormatterBuilder() minimumPrintedDigits(2) printZeroAlways() appendHours() appendSeparator(":") appendMinutes() appendSuffix(":") appendSeconds() toFormatter()
            return hms.print(diff)
          } catch {
            case oob: java.lang.IndexOutOfBoundsException => null
          }
        }
      } else
        null
  }
}
