package mecum

object AppWiring {
  val mecumDao: MecumSiteDaoImpl = new MecumSiteDaoImpl()
  val dataExtraction: DataExtractionImpl = new DataExtractionImpl()
  val carData: CarData = new CarData(mecumDao, dataExtraction)
}

