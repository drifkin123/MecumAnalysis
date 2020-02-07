package mecum

import org.jsoup.Connection

class AppWiring {

  val referrer = "https://www.mecum.com/infonet/search/reset/1/"
  val userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) " +
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"
  val websiteURLLogin: String = "https://www.mecum.com/infonet/login/"
  val searchPageURL: String = "https://www.mecum.com/infonet/search/reset/1"
  val baseURL: String = "https://www.mecum.com"

  val mecumDao: MecumSiteDaoImpl = new MecumSiteDaoImpl(
    referrer,
    userAgent,
    searchPageURL,
    baseURL,
    websiteURLLogin
  )

  val dataExtraction: DataExtractionImpl = new DataExtractionImpl()

  case class CarMeta(year: String, make: String, model: String, engine: String,
                     trans: String, miles: String, color: String, interior: String,
                     auctionResult: String, lot: String, auctionDate: String,
                     price: String)
}

