package mecum

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

import org.elasticsearch.spark._

class SparkConnection {
  val conf = new SparkConf().setAppName("mecum-analyzer2").setMaster("local")
  val sc = new SparkContext(conf)

  def insertToES(cars: Seq[Map[String, String]], carMake: String): Unit = {
    sc.makeRDD(cars).saveToEs(s"${carMake.trim.toLowerCase.replace(" ", "-")}/mecum")
  }
}
