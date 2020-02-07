package mecum

import org.jsoup.Connection
import org.jsoup.Connection.Response
import org.jsoup.nodes.{Document, Element, FormElement}
import org.jsoup.select.Elements


object App extends App {

  val appWiring = new AppWiring()
  val mecumDao: MecumSiteDaoImpl = appWiring.mecumDao
  val dataExtraction: DataExtractionImpl = appWiring.dataExtraction
  // Login to mecum
  val conn: Connection = mecumDao.connect(mecumDao.loginURL)
  val doc: Document = mecumDao.getDocument(conn)
  val form: FormElement = mecumDao.findForm("infonet-login", doc)
  var res: Response = mecumDao.submitForm(form, List(("email", "danrifkin@sbcglobal.net")))

  // Go to search page
  val searchPageConn: Document = mecumDao.submitSearchForm(res.cookies(), "past", "Ford")

  // Get links of all cars
  val hrefsOfAllCarsOnPage = mecumDao.hrefsForAllCarsOnPage(searchPageConn)

  // Prototype car link data extraction
  val linkToCar: String = mecumDao.baseURL + hrefsOfAllCarsOnPage(2)
  val carLinkDoc: Element = mecumDao.connect(linkToCar, res.cookies()).get().body()
  dataExtraction.extractData(carLinkDoc)

  dataExtraction.dataFromHrefs(hrefsOfAllCarsOnPage, mecumDao.baseURL)
}
