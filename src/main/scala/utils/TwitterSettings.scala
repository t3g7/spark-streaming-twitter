package utils

import scala.io.Source._

object TwitterSettings {

  /**
    * Set Twitter API credentials in src/main/resources/twitter_credentials.txt
    */
  def setTwitterCredentialsFromFile() = {
    val file = getClass().getResourceAsStream("twitter_credentials.txt")
    for (line <- fromInputStream(file).getLines()) {
      val key :: value :: _ = line.replace(" ","").split("=").toList
      val fullKey = "twitter4j.oauth." + key;
      System.setProperty(fullKey, value)
    }
  }

  /**
    * Get credentials provided as arguments at runtime
    * @param consumerKey
    * @param consumerSecret
    * @param accessToken
    * @param accessTokenSecret
    * @return
    */
  def setTwitterCredentialsFromArgs(consumerKey: String, consumerSecret: String, accessToken: String, accessTokenSecret: String) = {
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)
  }
}
