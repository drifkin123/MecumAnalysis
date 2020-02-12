package mecum

import org.apache.spark.sql.{SparkSession}
import org.elasticsearch.spark.sql

case class CarMeta(year: String, makeModel: String, engine: String,
                   trans: String, miles: String, color: String, interior: String,
                   auctionResult: String, lot: String, auctionLocation: String, auctionDate: String,
                   price: String)

class SparkConnection {

  val sparkSession = SparkSession.
    builder.
    master("local").
    appName("mecum-analyzer2").
    getOrCreate()

  import sparkSession.implicits._

  def mapToCaseClass(carMap: Map[String, String]): CarMeta = {
    CarMeta(
      carMap.getOrElse("year", ""), carMap.getOrElse("makeModel", ""), carMap.getOrElse("Engine", ""),carMap.getOrElse("Trans", ""),
      carMap.getOrElse("miles", ""), carMap.getOrElse("Color", ""), carMap.getOrElse("Interior", ""),carMap.getOrElse("AuctionResult", ""),
      carMap.getOrElse("lot", ""), carMap.getOrElse("auctionLocation", ""), carMap.getOrElse("auctionDate", ""),carMap.getOrElse("price", "")
    )
  }

  def insertCars(cars: List[Map[String, String]]) = {
    val mapOfCars: Seq[CarMeta] = cars.map(carMap => mapToCaseClass(carMap))
    val carMapDF = mapOfCars.toDF()
    carMapDF.show()


  }
}
