package utils

import scala.io.Source._
import org.apache.commons.cli.{PosixParser, Options}

import scala.tools.nsc.util.CommandLineParser.ParseException

object TwitterSettings {

  /**
    * Set Twitter API credentials in src/main/resources/twitter_credentials.txt
    */
  def setTwitterCredentialsFromFile() = {
    val file = getClass().getResourceAsStream("/twitter_credentials.txt")
    for (line <- fromInputStream(file).getLines()) {
      val key :: value :: _ = line.replace(" ","").split("=").toList
      val fullKey = "twitter4j.oauth." + key;
      System.setProperty(fullKey, value)
    }
  }

  /**
    * Get credentials provided as arguments at runtime
    * @param args
    * @return
    */
  def setTwitterCredentialsFromArgs(args: Array[String]) = {

    val CONSUMER_KEY = "consumerKey"
    val CONSUMER_SECRET = "consumerSecret"
    val ACCESS_TOKEN = "accessToken"
    val ACCESS_TOKEN_SECRET = "accessTokenSecret"

    val opts = {
      val options = new Options()
      options.addOption(CONSUMER_KEY, true, "Twitter API consumer key")
      options.addOption(CONSUMER_SECRET, true, "Twitter API consumer secret")
      options.addOption(ACCESS_TOKEN, true, "Twitter API access token")
      options.addOption(ACCESS_TOKEN_SECRET, true, "Twitter API access token secret")
      options
    }

    val parser = new PosixParser

    try {
      val cl = parser.parse(opts, args)
      System.setProperty("twitter4j.oauth.consumerKey", cl.getOptionValue(CONSUMER_KEY))
      System.setProperty("twitter4j.oauth.consumerSecret", cl.getOptionValue(CONSUMER_SECRET))
      System.setProperty("twitter4j.oauth.accessToken", cl.getOptionValue(ACCESS_TOKEN))
      System.setProperty("twitter4j.oauth.accessTokenSecret", cl.getOptionValue(ACCESS_TOKEN_SECRET))
      cl.getArgList.toArray
    } catch {
      case e: ParseException =>
        System.err.println("Parsing failed : " + e.getMessage)
        System.exit(1)
    }
  }
}
