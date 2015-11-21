import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest._

class SparkCountTest extends FlatSpec with BeforeAndAfter with GivenWhenThen with Matchers {
  private val master = "local[2]"
  private val appName = "spark-streaming-twitter"

  private var sc: SparkContext = _

  before {
    val conf = new SparkConf()
      .setMaster(master)
      .setAppName(appName)

    sc = new SparkContext(conf)
  }

  after {
    if (sc != null) {
      sc.stop()
    }
  }

  "Range" should "be counted" in {
    Given("range from 1 to 1000")
    val lines = 1 to 1000

    When("count")
    val distData = sc.parallelize(lines)
    val count = distData.count()

    Then("count 1000")
    count should be (1000)
  }
}
