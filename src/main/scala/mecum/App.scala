package mecum

import org.jsoup.Connection.Response
import org.jsoup.nodes.{Document}


object App extends App {

  val appWiring = new AppWiring()
  val mecumDao: MecumSiteDaoImpl = appWiring.mecumDao
  val dataExtraction: DataExtractionImpl = appWiring.dataExtraction

  var res: Response = mecumDao.login(List(("email", "danrifkin@sbcglobal.net")))

  // Go to search page
  val searchPageConn: Document = mecumDao.submitSearchForm(res.cookies(), "past", "Ford")
  val hrefsOfAllCarsOnPage = mecumDao.hrefsForAllCarsOnPage(searchPageConn)
  val carMapData = dataExtraction.dataFromHrefs(hrefsOfAllCarsOnPage, mecumDao.baseURL)
  println(carMapData)

//  val nextPageHref: String = mecumDao.hrefOfNextPage(searchPageConn)
//
//  val nextPage = mecumDao.connect(mecumDao.baseURL + nextPageHref, res.cookies()).get()
//  val hrefsOfAllPage2Cars = mecumDao.hrefsForAllCarsOnPage(nextPage)
//  val carMapData2 = dataExtraction.dataFromHrefs(hrefsOfAllPage2Cars, mecumDao.baseURL)
//  println(carMapData2)
  new SparkConnection().run()
  //new SparkConnection().insertCars(carMapData)

}
