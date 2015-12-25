import scala.io.Source._

object TwitterSettings {
  def setTwitterCredentialsFromFile() = {
    val file = getClass().getResourceAsStream("twitter_credentials.txt")
    for (line <- fromInputStream(file).getLines()) {
      val key :: value :: _ = line.replace(" ","").split("=").toList
      val fullKey = "twitter4j.oauth." + key;
      System.setProperty(fullKey, value)
    }
  }

  def setTwitterCredentialsFromArgs(consumerKey: String, consumerSecret: String, accessToken: String, accessTokenSecret: String) = {
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)
  }
}
