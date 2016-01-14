package ml.naivebayes

import org.apache.spark.SparkConf
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import utils.TwitterSettings

object PredictFromStreamNB {
  def main(args: Array[String]) {
    if (args.length == 0) {
      System.err.println("Usage: " + this.getClass.getSimpleName + " --consumerKey <consumer key> --consumerSecret <consumer secret> --accessToken <access token> --accessTokenSecret <access token secret> <trainedModel directory>")
      System.exit(1)
    }

    val trainedModelDir = args(8)
    TwitterSettings.setTwitterCredentialsFromArgs(args)

    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("TwitterStreaming Sentiment Prediction with Naive Bayes classifier")
    val ssc = new StreamingContext(conf, Seconds(5))

    // Select some words, accounts or hashtags to stream
    val filters = Seq("orange", "orange_france", "sosh", "sosh_fr", "orange_conseil")

    // Filter out tweets containing off topic words or hashtags
    val offTopicWords = Seq("jus", "juice", "alert", "alerte", "OINTB", "#OINTB", "black", "vigilance")
    val offTopicHashtags = Seq("#alert", "#alerte", "#vigilance", "#OINTB", "OrangeIsTheNewBlack")
    val offTopicFilters = offTopicWords ++ offTopicHashtags

    val stream = TwitterUtils.createStream(ssc, None, filters).filter(_.getLang == "fr")
    val tweets = stream.filter(_.isRetweet == false)
                       .filter(_.getText.split(" ").exists(offTopicFilters contains _) == false)
    val statuses = tweets.map(_.getText)

    val model = NaiveBayesModel.load(ssc.sparkContext, trainedModelDir.toString)
    val labeledStatuses = statuses.map(t => (t, model.predict(TrainNB.featurize(t))))

    labeledStatuses.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
