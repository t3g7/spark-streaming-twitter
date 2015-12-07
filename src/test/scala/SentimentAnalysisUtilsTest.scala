import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import utils.SentimentAnalysisUtils._

class SentimentAnalysisUtilsTest extends FunSpec with ShouldMatchers{
  describe("A sentiment analysis utility class") {
    it ("should detect not understood statement") {
      detectSentiment("") should equal (NOT_UNDERSTOOD)
    }

    it ("should detect a negative statement 1") {
      detectSentiment("Wifi down @virginmedia  & thanks to @o2 no back up with a phone signal in QUORN - #1stworldproblems I know.") should equal (NEGATIVE)
    }

    it ("should detect a negative statement 2") {
      detectSentiment("Thanks @O2 phone merrily allowed to go beyond data allowance. No capping! Extortionate bill at Xmas brilliant service.") should equal (NEGATIVE)
    }

    it ("should detect a negative statement 3") {
      detectSentiment("@O2 just 'upgraded'. Shame my upgrade doesn't work properly! Absolutely livid!") should equal (NEGATIVE)
    }

    it ("should detect a neutral statement 4") {
      detectSentiment("@O2 iPhone 6s") should equal (NEUTRAL)
    }

    it ("should detect a neutral statement") {
      detectSentiment("@O2 has it been removed?") should equal (NEUTRAL)
    }

    it ("should detect a positive statement 1") {
      detectSentiment("@O2 Great giveaway #O2CyberMonday") should equal (POSITIVE)
    }

    it ("should detect a positive statement 2") {
      detectSentiment("@O2 wow what Amazing Bundle Would make my Christmas #O2CyberMonday") should equal (POSITIVE)
    }

    it ("should detect a positive statement 3") {
      detectSentiment("@O2 Looks fantastic - very generous!  #O2CyberMonday") should equal (POSITIVE)
    }

    /*it ("should detect a positive statement 4") {
      detectSentiment("@o2 just made my day, little stars they are") should equal (POSITIVE)
    }*/

    it ("should detect a very positive statement 1") {
      detectSentiment("@O2 Wow \uD83D\uDE0D fantastic prize, u guys are so so kind, got my fingers tightly crossed \uD83D\uDE00") should equal (VERY_POSITIVE)
    }

    /*it ("should detect a very positive statement 2") {
      detectSentiment("@O2  Wow what a fab prize!!! \uD83C\uDF81 #O2CyberMonday") should equal (VERY_POSITIVE)
    }*/


  }
}
