import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import utils.SentimentAnalysisUtils._

class SentimentAnalysisUtilsTest extends FunSpec with ShouldMatchers{
  describe("A sentiment analysis utility class") {
    it ("should detect not understood statement") {
      detectSentiment("") should equal (NOT_UNDERSTOOD)
    }

    it ("should detect a negative statement") {
      detectSentiment("I am feeling very sad and frustrated.") should equal (NEGATIVE)
    }

    it ("should detect a neutral statement") {
      detectSentiment("I'm watching a movie") should equal (NEUTRAL)
    }

    it ("should detect a positive statement") {
      detectSentiment("C'était une bonne expérience.") should equal (POSITIVE)
    }

    it ("should detect a very positive statement") {
      detectSentiment("It was a very nice experience.") should equal (VERY_POSITIVE)
    }

    it("should detect a negative sentiment on a full review") {
      detectSentiment(
        """
          |This movie doesn't care about cleverness, wit or any other kind of intelligent humor.
          |Those who find ugly meanings in beautiful things are corrupt without being charming.
          |There are slow and repetitive parts, but it has just enough spice to keep it interesting.
        """.stripMargin) should equal (NEGATIVE)
    }
  }
}
