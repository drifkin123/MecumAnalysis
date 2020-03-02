package mecum

object AppWiring {
  val logger: Logger = new Logger()
  val mecumDao: MecumSiteDaoImpl = new MecumSiteDaoImpl()
  val dataExtraction: DataExtractionImpl = new DataExtractionImpl()
  val sc = new SparkConnection()
  val carData: CarData = new CarData(mecumDao, dataExtraction, sc)
}

