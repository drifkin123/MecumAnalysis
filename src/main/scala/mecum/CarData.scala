package mecum

import mecum.AppWiring.{carData, mecumDao}
import org.jsoup.Connection.Response
import org.jsoup.nodes.{Document, Element}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

class CarData(mecumDao: MecumSiteDaoImpl, dataExtraction: DataExtractionImpl, sc: SparkConnection) {

  def getCarsByMake(make: String) = {
    val res: Response = mecumDao.login(List(("email", "danrifkin@sbcglobal.net")))
    val searchPageConn: Document = mecumDao.submitSearchForm(res.cookies(), "past", make)
    val hrefsOfAllCarsOnPage = mecumDao.hrefsForAllCarsOnPage(searchPageConn)
    val esCarData = carData.getDataFromHrefs(hrefsOfAllCarsOnPage, mecumDao.baseURL, res)

    sc.insertToES(esCarData, make)

    val possibleNextPageHref: Option[String] = mecumDao.hrefOfNextPage(searchPageConn)
    carsForNextPages(possibleNextPageHref, res, make)
  }

  def carsForNextPages(nextPageHref: Option[String], res: Response, make: String): Unit = {
    println(s"Next page: $nextPageHref")
    nextPageHref match {
      case Some(href) => {
        val nextPage = mecumDao.connect(mecumDao.baseURL + href, res.cookies()).get()
        val hrefsOfAllPage2Cars = mecumDao.hrefsForAllCarsOnPage(nextPage)
        val esCarData = carData.getDataFromHrefs(hrefsOfAllPage2Cars, mecumDao.baseURL, res)
        sc.insertToES(esCarData, make)
        val thePageAfterThis = mecumDao.hrefOfNextPage(nextPage)
        carsForNextPages(thePageAfterThis, res, make)
      }
      case _ => {
        println("Done...")
      }
    }
  }

  def getDataFromHrefs(hrefs: List[String], baseURL: String, res: Response): Seq[Map[String, String]] = {
    val requestPageElements = dataFromHrefs(hrefs, baseURL, res)
    val resolvedPageElements = getDataFromFuturesAwait(requestPageElements)
    val extractedCarData = getDataFromResolvedFutures(resolvedPageElements)
    extractedCarData
  }

  def dataFromHrefs(hrefs: List[String], baseURL: String, res: Response): List[Future[(Element, String)]] = {
    val listOfFutures = hrefs match {
      case List() => List()
      case href :: rest => {
        val linkToCar: String = baseURL + href
        val carLinkDoc: Future[(Element, String)] = Future {
          (mecumDao.connect(linkToCar, res.cookies()).get().body(), linkToCar)
        }
        carLinkDoc :: dataFromHrefs(rest, baseURL, res)
      }
    }

    listOfFutures;
  }

  def lift[T](futures: Seq[Future[T]]): Seq[Future[Try[T]]] =
    futures.map(_.map {
      Success(_)
    }.recover { case t => Failure(t) })

  def getDataFromFuturesAwait(futures: Seq[Future[(Element, String)]]): Seq[Try[(Element, String)]] = {
    Await.result(Future.sequence(lift(futures)), Duration.Inf)
  }

  def getDataFromResolvedFutures(resolvedFutures: Seq[Try[(Element, String)]]): Seq[Map[String, String]] = {
    resolvedFutures.map(f => {
      val extractedData = dataExtraction.extractData(f.get._1) + ("link" -> f.get._2)
      println("--------------------------")
      println(extractedData)
      println()
      extractedData
    })
  }
}
