package ml

import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkContext, SparkConf}
import utils.SentimentAnalysisUtils.loadWordSet

object Train {
  def main(args: Array[String]) {
    if (args.length == 0) {
      System.err.println("Usage: " + this.getClass.getSimpleName + " <training file>")
      System.exit(1)
    }

    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("Twitter Sentiment Trainer")
    val sc = new SparkContext(conf)

    val stopWords = sc.broadcast(loadWordSet("/wordsets/stop-words.txt")).value

    // Load the labeled training data
    val rawData = sc.textFile(args(0))
    val header = rawData.first()
    val data = rawData.filter(x => x != header)

    // Split data between training and testing data
    val splitData = data.randomSplit(Array(0.8, 0.2), 11L)
    val training = splitData(0)
    val test = splitData(1)


    val training_labeled = training.map(x => toLabels(x)).
      map(t => (t._1, featurize(t._2))).
      map(x => new LabeledPoint((x._1).toDouble, x._2))

    println("\n====== Training ======\n")
    val model = time {NaiveBayes.train(training_labeled, 1.0)}

    println("\n====== Testing ======\n")
    val testing_labeled = test.map(x => toLabels(x)).
      map(t => (t._1, featurize(t._2), t._2)).
      map(x => {
        val lp = new LabeledPoint((x._1).toDouble, x._2)
        (lp, x._3)
      })

    val predictAndLabel = time {
      testing_labeled.map(p => {
        val labeledPoint = p._1
        val text = p._2
        val features = labeledPoint.features
        val actual_label = labeledPoint.label
        val predicted_label = model.predict(features)
        (actual_label, predicted_label, text)
      })
    }

    val accuracy = 1.0 * predictAndLabel.filter(x => x._1 == x._2).count() / test.count()

    println("Training and testing complete. Accuracy = " + accuracy)
    println("\nSome predictions:\n")

    predictAndLabel.take(10).foreach(x => {
      println("---------------------------------------------------------------")
      println("Text = " + x._3)
      println("Actual label = " + (if (x._1 == 1) "positive" else "negative"))
      println("Predicted label = " + (if (x._2 == 1) "positive" else "negative"))
      println("---------------------------------------------------------------\n")
    })

    if (args.length == 2) {
      val savePath = args(1) + "/" + accuracy.toString
      model.save(sc, args(1))
      println("\n====== Model saved to: " + savePath + "\n")
    }

    sc.stop()
    println("\n====== Successfully stopped Spark Context, exiting.")
  }

  def toLabels(line: String) = {
    val col = line.split(',')
    (col(1), col(2))
  }

  /**
    * https://databricks.gitbooks.io/databricks-spark-reference-applications/content/twitter_classifier/train.html
    * Create feature vectors by turning each tweet into bigrams of
    * characters (an n-gram model) and then hashing those to a
    * length-1000 feature vector that we can pass to MLlib.
    * This is a common way to decrease the number of features in a
    * model while still getting excellent accuracy. (Otherwise every
    * pair of Unicode characters would potentially be a feature.)
    * @param s
    * @return
    */
  def featurize(s: String): Vector = {
    val numFeatures = 1000
    val tf = new HashingTF(numFeatures)

    tf.transform(s.sliding(2).toSeq)
  }

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    println("\nElapsed time: " + (t1 - t0)/1000 + "ms")
    result
  }
}
