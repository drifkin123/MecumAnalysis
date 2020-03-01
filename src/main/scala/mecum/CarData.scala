package mecum

import mecum.AppWiring.{carData, mecumDao}
import org.jsoup.Connection.Response
import org.jsoup.nodes.{Document, Element}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

class CarData(mecumDao: MecumSiteDaoImpl, dataExtraction: DataExtractionImpl) {

  def getCarsByMake(make: String) = {
    val res: Response = mecumDao.login(List(("email", "danrifkin@sbcglobal.net")))
    val searchPageConn: Document = mecumDao.submitSearchForm(res.cookies(), "past", make)
    val hrefsOfAllCarsOnPage = mecumDao.hrefsForAllCarsOnPage(searchPageConn)

    carData.getDataFromHrefs(hrefsOfAllCarsOnPage, mecumDao.baseURL, res)

    val possibleNextPageHref: Option[String] = mecumDao.hrefOfNextPage(searchPageConn)
    carsForNextPages(possibleNextPageHref, res)
  }

  def carsForNextPages(nextPageHref: Option[String], res: Response): Unit = {
    println(s"Next page: $nextPageHref")
    nextPageHref match {
      case Some(href) => {
        val nextPage = mecumDao.connect(mecumDao.baseURL + href, res.cookies()).get()
        val hrefsOfAllPage2Cars = mecumDao.hrefsForAllCarsOnPage(nextPage)
        val carMapData2 = carData.getDataFromHrefs(hrefsOfAllPage2Cars, mecumDao.baseURL, res)
        val thePageAfterThis = mecumDao.hrefOfNextPage(nextPage)
        carsForNextPages(thePageAfterThis, res)
        // Insert carMapData to ES
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

  def dataFromHrefs(hrefs: List[String], baseURL: String, res: Response): List[Future[Element]] = {
    val listOfFutures = hrefs match {
      case List() => List()
      case href :: rest => {
        val linkToCar: String = baseURL + href
        val carLinkDoc: Future[Element] = Future {
          mecumDao.connect(linkToCar, res.cookies()).get().body()
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

  def getDataFromFuturesAwait(futures: Seq[Future[Element]]): Seq[Try[Element]] = {
    Await.result(Future.sequence(lift(futures)), Duration.Inf)
  }

  def getDataFromResolvedFutures(resolvedFutures: Seq[Try[Element]]): Seq[Map[String, String]] = {
    resolvedFutures.map(f => {
      val extractedData = dataExtraction.extractData(f.get)
      println("--------------------------")
      println(extractedData)
      println()
      extractedData
    })
  }
}
