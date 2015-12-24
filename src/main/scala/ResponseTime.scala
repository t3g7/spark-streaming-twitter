import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkConf
import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.{DateTime, Period}

object ResponseTime {
  def getResponseTime(conf : SparkConf, inReplyToStatusId : Long, timestamp : DateTime ): String = {
    if(inReplyToStatusId > 0) {
      CassandraConnector(conf).withSessionDo { session =>
        val reply_id = session.execute("SELECT created_at FROM twitter_streaming.tweets WHERE tweet_id="+ inReplyToStatusId +" ALLOW FILTERING")
        try {
          val reply_time_it = Option(reply_id.all().get(0).getTimestamp("created_at")).getOrElse()
          val t2 = new DateTime(reply_time_it)
          val diff = new Period(t2, timestamp)
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
