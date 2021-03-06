package utils

import java.io.{InputStream, StringReader}

import edu.stanford.nlp.international.french.process.FrenchTokenizer

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source._

object SentimentAnalysisUtils {

  val tokenizerFactory = FrenchTokenizer.ftbFactory()

  /**
    * Tokenize a tweet text and filter out stop words
    * @param content
    * @param stopWords
    * @return a sequence of tokens
    */
  def tokenize(content: String, stopWords: Set[String]): Seq[String] = {
    val stringReader = new StringReader(content)
    val tokenizer = tokenizerFactory.getTokenizer(stringReader)

    val tokens = new ArrayBuffer[String]()
    for (t <- tokenizer) {
      val token = t.toString
      if (!stopWords.contains(token.toLowerCase) && token.length > 1) {
        tokens += token
      }
    }
    println("DEBUG - tokens: " + tokens)
    tokens
  }

  /**
    * Load a newline-separated words set from a text file
    * @param path
    * @return a set of words
    */
  def loadWordSet(path: String) = {
    val stream: InputStream = getClass.getResourceAsStream(path)
    val lines = fromInputStream(stream).getLines
    lines.toSet
  }

  /**
    * Compute the weight of a tweet by finding a token in a words set
    * @param tokens
    * @param wordSet
    * @return an integer value
    */
  def countWeight(tokens: Seq[String], wordSet: Set[String]): Int = {
    var weight = 0
    for (token <- tokens) {
      if (wordSet.contains(token)) {
        weight += 1
      }
    }
    weight
  }

  /**
    * Compute a sentiment based on the weight of the tweet
    * @param text
    * @return a SENTIMENT_TYPE string in {NEGATIVE, NEUTRAL, POSITIVE}
    */
  def detectSentiment(text: String): SENTIMENT_TYPE = {

    val posWordsSet = loadWordSet("/wordsets/pos-words.txt")
    val negWordsSet = loadWordSet("/wordsets/neg-words.txt")
    val stopWordsSet = loadWordSet("/wordsets/stop-words.txt")

    val tokens = tokenize(text, stopWordsSet)

    val posWordWeight = countWeight(tokens, posWordsSet)
    val negWordWeight = countWeight(tokens, negWordsSet)
    println("DEBUG - pos weight: " + posWordWeight)
    println("DEBUG - neg weight: " + negWordWeight)

    val sentiment = posWordWeight - negWordWeight
    println("DEBUG - sentiment score = " + sentiment)

    sentiment match {
      case s if s > 0 => POSITIVE
      case s if s < 0 => NEGATIVE
      case s if s == 0 => NEUTRAL
    }
  }

  trait SENTIMENT_TYPE
  case object NEGATIVE extends SENTIMENT_TYPE
  case object NEUTRAL extends SENTIMENT_TYPE
  case object POSITIVE extends SENTIMENT_TYPE
}
