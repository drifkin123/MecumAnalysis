package mecum

// import org.apache.spark.sql.{SparkSession}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import org.elasticsearch.spark._

case class CarMeta(year: String, makeModel: String, engine: String,
                   trans: String, miles: String, color: String, interior: String,
                   auctionResult: String, lot: String, auctionLocation: String, auctionDate: String,
                   price: String)

class SparkConnection {

//  val sparkSession = SparkSession.
//    builder.
//    master("local").
//    appName("mecum-analyzer2").
//    getOrCreate()
//
//  import sparkSession.implicits._
  val conf = new SparkConf().setAppName("mecum-analyzer2").setMaster("local")
  val sc = new SparkContext(conf)
  //val numbers = Map("one" -> 1, "two" -> 2, "three" -> 3)
  //val airports = Map("arrival" -> "Otopeni", "SFO" -> "San Fran")

  //sc.makeRDD(
  //  Seq(numbers, airports)
  //).saveToEs("spark/docs")


  def run(cars: Seq[Map[String, String]]): Unit = {
    sc.makeRDD(cars).saveToEs("mecum")
  }

  def mapToCaseClass(carMap: Map[String, String]): CarMeta = {
    CarMeta(
      carMap.getOrElse("year", ""), carMap.getOrElse("makeModel", ""), carMap.getOrElse("Engine", ""),carMap.getOrElse("Trans", ""),
      carMap.getOrElse("miles", ""), carMap.getOrElse("Color", ""), carMap.getOrElse("Interior", ""),carMap.getOrElse("AuctionResult", ""),
      carMap.getOrElse("lot", ""), carMap.getOrElse("auctionLocation", ""), carMap.getOrElse("auctionDate", ""),carMap.getOrElse("price", "")
    )
  }



//  def insertCars(cars: List[Map[String, String]]) = {
//    val mapOfCars: Seq[CarMeta] = cars.map(carMap => mapToCaseClass(carMap))
//    val carMapDF = mapOfCars.toDF()
//    carMapDF.show()
//  }

}
