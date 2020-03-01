package mecum

object App extends App {
  val carData = AppWiring.carData

  println("Starting timer...")
  val time = new java.util.Date().getTime

  carData.getCarsByMake("AJS")

  println(s"THIS TOOK: ${(new java.util.Date().getTime - time) / 1000.0}")

  // new SparkConnection().run(carMapData)
  // new SparkConnection().insertCars(carMapData)

}
