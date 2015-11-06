import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._

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

    val conf = new SparkConf().setMaster("local[2]").setAppName("WordCount")
    val ssc = new StreamingContext(conf, Seconds(1))

    val filters = Seq("orange", "orange_france", "sosh", "sosh_fr", "orange_conseil")

    val stream = TwitterUtils.createStream(ssc, None, filters)

    // Statuses
    val statuses = stream.map(status => status.getText())
    statuses.print()

    // Hashtags
    /*
    val hashTags = stream.flatMap(status => status.getText.split(" ").filter(_.startsWith("#")))

    val topCounts60 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(60))
      .map{case (topic, count) => (count, topic)}
      .transform(_.sortByKey(false))

    // Print popular hashtags
    topCounts60.foreachRDD(rdd => {
      val topList = rdd.take(5)
      println("\nPopular topics in last 60 seconds (%s total):".format(rdd.count()))
      topList.foreach{case (count, tag) => println("%s (%s tweets)".format(tag, count))}
    })
    */

    ssc.start()
    ssc.awaitTermination()
  }
}
