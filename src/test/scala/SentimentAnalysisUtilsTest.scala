import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import utils.SentimentAnalysisUtils._

class SentimentAnalysisUtilsTest extends FunSpec with ShouldMatchers{
  describe("A sentiment analysis utility class") {
    it ("should detect a negative statement") {
      detectSentiment("@Sosh_fr iPhone volé, je fais quoi maintenant ?") should equal (NEGATIVE)
    }

    it ("should detect a positive statement") {
      detectSentiment("Merci @Orange_France @Orange_conseil @orange depuis que je suis chez vous je suis l'homme le plus heureux") should equal (POSITIVE)
    }
  }
}
