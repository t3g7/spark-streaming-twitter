import com.datastax.spark.connector.SomeColumns
import org.apache.spark._
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Time, Seconds, StreamingContext}
import org.joda.time.{DateTimeZone, DateTime}
import com.datastax.spark.connector.streaming._

import scala.io.Source._


object Streamer {
  def configureTwitterCredentials() = {
    val file = getClass().getResourceAsStream("twitter_credentials.txt")
    for (line <- fromInputStream(file).getLines()) {
      val key :: value :: _ = line.replace(" ","").split("=").toList
      val fullKey = "twitter4j.oauth." + key;
      System.setProperty(fullKey, value)
    }
  }

  def main(args: Array[String]) {

    configureTwitterCredentials()

    val conf = new SparkConf()
                      .setMaster("local[2]")
                      .setAppName("TwitterStreamer")
                      .set("spark.cassandra.connection.host", "localhost")
    val ssc = new StreamingContext(conf, Seconds(1))

    val filters = Seq("orange", "orange_france", "sosh", "sosh_fr", "orange_conseil")
    val stream = TwitterUtils.createStream(ssc, None, filters).filter(_.getLang == "fr")

    stream.map(t => t.getText)
      .countByValueAndWindow(Seconds(5), Seconds(5))
      .transform((rdd, time) => rdd.map { case (term, count) => (term, count, now(time))})
      .saveToCassandra("twitter_streaming", "tweets", SomeColumns("body", "mentions", "interval"))

    ssc.checkpoint("./checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }

  private def now(time: Time): String =
    new DateTime(time.milliseconds, DateTimeZone.UTC ).toString("yyyyMMddHH:mm:ss")
}
