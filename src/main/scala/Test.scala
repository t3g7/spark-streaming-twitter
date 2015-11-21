import org.apache.spark._
import org.apache.spark.streaming._

object Test {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[2]").setAppName("WordCount")
    val ssc = new StreamingContext(conf, Seconds(1))

    val lines = ssc.socketTextStream("localhost", 7777)
    val errorLines = lines.filter(_.contains("error"))
    errorLines.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
