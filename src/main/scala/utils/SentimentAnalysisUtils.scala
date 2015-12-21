package utils

import java.io.{InputStream, StringReader}
import java.util.Properties

import edu.stanford.nlp.international.french.process.FrenchTokenizer
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

import scala.collection.JavaConversions._
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.io.Source._

object SentimentAnalysisUtils {

  val tokenizerFactory = FrenchTokenizer.ftbFactory()

  def filterAndTokenizeFromStopWords(content: String, stopWords: Set[String]): Seq[String] = {
    val stringReader = new StringReader(content)
    val tokenizer = tokenizerFactory.getTokenizer(stringReader)

    val tokens = new ArrayBuffer[String]()
    tokenizer.filter(t => t.toString.length > 1 && !stopWords.contains(t.toString)).foreach(t => tokens += t.toString)

    println(tokens)
    tokens
  }

  def tokenize(content: String): Unit = {
    filterAndTokenizeFromStopWords(content, loadStopWords("/dictionnaries/stop-words.txt"))
  }

  def loadStopWords(path: String) = {
    val stream: InputStream = getClass.getResourceAsStream(path)
    val lines = fromInputStream(stream).getLines
    lines.toSet
  }

  val nlpProps = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")
    props
  }

  def detectSentiment(message: String): SENTIMENT_TYPE = {

    val pipeline = new StanfordCoreNLP(nlpProps)

    val annotation = pipeline.process(message)
    var sentiments: ListBuffer[Double] = ListBuffer()
    var sizes: ListBuffer[Int] = ListBuffer()

    var longest = 0
    var mainSentiment = 0

    for (sentence <- annotation.get(classOf[CoreAnnotations.SentencesAnnotation])) {
      val tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
      val sentiment = RNNCoreAnnotations.getPredictedClass(tree)
      val partText = sentence.toString

      if (partText.length > longest) {
        mainSentiment = sentiment
        longest = partText.length
      }

      sentiments += sentiment.toDouble
      sizes += partText.length

      println("debug: " + sentiment)
      println("size: " + partText.length)
    }

    val averageSentiment:Double = {
      if (sentiments.size > 0) {
        sentiments.sum / sentiments.size
      }
      else {
        -1
      }
    }

    val weightedSentiments = (sentiments, sizes).zipped.map((sentiment, size) => sentiment * size)
    var weightedSentiment = weightedSentiments.sum / (sizes.fold(0)(_ + _))

    if (sentiments.size == 0) {
      mainSentiment = -1
      weightedSentiment = -1
    }

    println("debug: main: " + mainSentiment)
    println("debug: avg: " + averageSentiment)
    println("debug: weighted " + weightedSentiment)

    /*
      0 : very negative
      1 : negative
      2 : neutral
      3 : positive
      4 : very positive
     */
    weightedSentiment match {
      case s if s <= 0.0 => NOT_UNDERSTOOD
      case s if s < 1.0 => VERY_NEGATIVE
      case s if s < 2.0 => NEGATIVE
      case s if s < 3.0 => NEUTRAL
      case s if s < 4.0 => POSITIVE
      case s if s < 5.0 => VERY_POSITIVE
      case s if s > 5.0 => NOT_UNDERSTOOD
    }
  }

  trait SENTIMENT_TYPE
  case object VERY_NEGATIVE extends SENTIMENT_TYPE
  case object NEGATIVE extends SENTIMENT_TYPE
  case object NEUTRAL extends SENTIMENT_TYPE
  case object POSITIVE extends SENTIMENT_TYPE
  case object VERY_POSITIVE extends SENTIMENT_TYPE
  case object NOT_UNDERSTOOD extends SENTIMENT_TYPE
}
