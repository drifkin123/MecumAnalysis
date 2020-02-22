package mecum

import org.jsoup.Connection.Response
import org.jsoup.nodes.{Document}


object App extends App {

  val appWiring = new AppWiring()
  val mecumDao: MecumSiteDaoImpl = appWiring.mecumDao
  val dataExtraction: DataExtractionImpl = appWiring.dataExtraction

  var res: Response = mecumDao.login(List(("email", "danrifkin@sbcglobal.net")))

  // Go to search page
  val searchPageConn: Document = mecumDao.submitSearchForm(res.cookies(), "past", "AJS")
  val hrefsOfAllCarsOnPage = mecumDao.hrefsForAllCarsOnPage(searchPageConn)
  val carMapData: Seq[Map[String, String]] = dataExtraction.dataFromHrefs(hrefsOfAllCarsOnPage, mecumDao.baseURL).toSeq

  val nextPageHref: Option[String] = mecumDao.hrefOfNextPage(searchPageConn)
  println(nextPageHref)

  carsForNextPage(nextPageHref)

  def carsForNextPage(nextPageHref: Option[String]): Unit = {
    println(s"Next page: $nextPageHref")
    nextPageHref match {
      case Some(href) => {
        val nextPage = mecumDao.connect(mecumDao.baseURL + href, res.cookies()).get()
        val hrefsOfAllPage2Cars = mecumDao.hrefsForAllCarsOnPage(nextPage)
        val carMapData2 = dataExtraction.dataFromHrefs(hrefsOfAllPage2Cars, mecumDao.baseURL)
        val thePageAfterThis = mecumDao.hrefOfNextPage(nextPage)
        carsForNextPage(thePageAfterThis)
        // Insert carMapData to ES
      }
      case _ => {
        println("Done...")
      }
    }
  }

  // new SparkConnection().run(carMapData)
  //new SparkConnection().insertCars(carMapData)

}
