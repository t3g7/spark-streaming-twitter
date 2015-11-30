import com.datastax.spark.connector.streaming._
import twitter4j._

object StreamUser {

  object Util {
    val config = new twitter4j.conf.ConfigurationBuilder()
      .setOAuthConsumerKey("")
      .setOAuthConsumerSecret("")
      .setOAuthAccessToken("")
      .setOAuthAccessTokenSecret("")
      .build
  }

  def simpleStatusListener = new StatusListener() {
    def onStatus(status: Status) {
      println(status.getUser.getScreenName + " : " + status.getText + " at " + status.getCreatedAt + " in response to tweet id #" + status.getInReplyToStatusId)

      val cassandraRDD = TwitterStreamingApp.ssc.cassandraTable("twitter_streaming", "tweets").select("created_at").where("tweet_id = ?", status.getInReplyToStatusId)
      cassandraRDD.foreach(println)
    }

    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {
      println("Status deletion notice")
    }

    def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {
      println("Track limitation notice for " + numberOfLimitedStatuses)
    }

    def onException(ex: Exception) {
      println("Exception occured" + ex.getMessage)
    }

    def onScrubGeo(arg0: Long, arg1: Long): Unit = {
      println("Scrub geo with : " + arg0 + ":" + arg1)
    }

    def onStallWarning(warning: StallWarning): Unit = {
      println("Stall warning")
    }
  }
}
