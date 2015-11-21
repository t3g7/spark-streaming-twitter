import scala.io.Source._

object TwitterSettings {
  def configureTwitterCredentials() = {
    val file = getClass().getResourceAsStream("twitter_credentials.txt")
    for (line <- fromInputStream(file).getLines()) {
      val key :: value :: _ = line.replace(" ","").split("=").toList
      val fullKey = "twitter4j.oauth." + key;
      System.setProperty(fullKey, value)
    }
  }
}
