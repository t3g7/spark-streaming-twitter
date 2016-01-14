package ml.naivebayes

import java.text.SimpleDateFormat
import java.util.Calendar

import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkConf, SparkContext}

object TrainNB {
  def main(args: Array[String]) {
    if (args.length == 0) {
      System.err.println("Usage: " + this.getClass.getSimpleName + " <training file> <trained model save folder>")
      System.exit(1)
    }

    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("Twitter Sentiment Trainer with Naive Bayes classifier")
    val sc = new SparkContext(conf)

    // Load the labeled training data
    val rawData = sc.textFile(args(0))
    val header = rawData.first()
    val data = rawData.filter(x => x != header)

    // Split data between training and testing data
    val splits = data.randomSplit(Array(0.8, 0.2))
    val (trainingData, testData) = (splits(0), splits(1))

    val trainingLabeledData = trainingData.map(x => toLabels(x)).
      map(t => (t._1, featurize(t._2))).
      map(x => new LabeledPoint((x._1).toDouble, x._2))

    println("\n====== Training ======\n")
    val model = time {NaiveBayes.train(trainingLabeledData, 1.0)}

    println("\n====== Testing ======\n")
    val testingLabeledData = testData.map(x => toLabels(x)).
      map(t => (t._1, featurize(t._2), t._2)).
      map(x => {
        val lp = new LabeledPoint((x._1).toDouble, x._2)
        (lp, x._3)
      })

    val predictAndLabel = time {
      testingLabeledData.map(p => {
        val labeledPoint = p._1
        val text = p._2
        val features = labeledPoint.features
        val actualLabel = labeledPoint.label
        val predictedLabel = model.predict(features)
        (actualLabel, predictedLabel, text)
      })
    }

    val accuracy = 1.0 * predictAndLabel.filter(x => x._1 == x._2).count() / testData.count()

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
      val format = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss")
      val now = format.format(Calendar.getInstance().getTime())

      val savePath = args(1) + "_" + now
      model.save(sc, savePath)
      println("\nModel saved to: " + savePath + "\n")
    }

    sc.stop()
    println("Successfully stopped Spark Context, exiting.")
  }

  def toLabels(line: String) = {
    val col = line.split(';')
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
    println("Elapsed time: " + (t1 - t0)/1000000 + " ms")
    result
  }
}
