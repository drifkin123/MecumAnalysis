package mecum

import mecum.App.{mecumDao, res}
import org.jsoup.nodes.Element

class DataExtractionImpl {

  def extractAuctionInfo(el: Element): Map[String, String] = {
    val lotAuctionInfo = Option(el.selectFirst("h3.lot-auction-info"))
    val elRegex = raw"(Lot [^ ]+) ([^0-9]+) (.*)".r

    lotAuctionInfo match {
      case Some(auctionInfoEl) => auctionInfoEl.text match {
          case elRegex(lot, location, date) => Map(
            "Lot" -> lot,
            "AuctionLocation" -> location,
            "AuctionDate" -> date)
          case _ => Map()
        }
      case _ => Map()
    }
  }

  def extractBidStatus(el: Element): Map[String, String] = {
    val options: List[String] = List("bid-goes-on", "sold", "high-bid")
    val elRegex = """class="(.*)"""".r
    val auctionResults = Option(el.selectFirst("div.auction-results"))

    auctionResults match {
      case Some(auctionResultsEl) => {
        val attributes: String = auctionResultsEl.attributes().toString.trim

        attributes match {
          case elRegex(results) => {
            val bidStatus = results.split(" ").filter(options.contains(_)).toList.lift(0)
            bidStatus match {
              case Some(status) => Map("AuctionStatus" -> status)
              case _ => Map()
            }
          }
          case _ => Map()
        }
      }
      case _ => Map()
    }
  }

  def extractCarType(el: Element): Map[String, String] = {
    val lot = Option(el.selectFirst("h1.lot-title"))
    val elRegex = """([\d]+) (.*)""".r

    lot match {
      case Some(lotEl) => lotEl.text() match {
          case elRegex(year, car) => Map("CarModel" -> car, "CarYear" -> year)
          case _ => Map()
        }
      case _ => Map()
    }
  }

  def extractPrice(el: Element): Map[String, String] = {
    val price: Option[Element] = Option(el.selectFirst("span.lot-price"))

    price match {
      case Some(priceEl) => Map("CarPrice" -> priceEl.text())
      case _ => Map()
    }
  }

  def genMap(carComponents: List[String]): Map[String, String] = {
    val elRegex = """<li><h5>(.+)<\/h5>(.+)<\/li>""".r

    carComponents match {
      case List() => Map()
      case components :: rest => {
        components match {
          case elRegex(key, value) => Map(key -> value) ++ genMap(rest)
          case _ => Map()
        }
      }
    }
  }

  def extractLotBreakdown(el: Element): Map[String, String] = {
    val lotBreakdownEl = Option(el.selectFirst(".lot-breakdown-list"))

    lotBreakdownEl match {
      case Some(logBreakdown) => {
        val children = logBreakdown.children()
        val lotInfo = children.toString.split("\n").toList
        genMap(lotInfo)
      }
      case _ => Map()
    }

  }

  def extractMiles(el: Element): Map[String, String] = {
    val milesEl = Option(el.selectFirst("ul.lot-highlights"))
    val milesLiRegex = """.*[<li>| ]([0-9,]+) miles.*""".r

    milesEl match {
      case Some(highlights) => {
        val highlightList: List[String] = highlights.children().toString.split("\n").toList
        val milesLi = highlightList.filter(_.contains("miles")).lift(0)
        milesLi match {
          case Some(milesLiEl) => {
            milesLiEl match {
              case milesLiRegex(miles) => Map("CarMiles" -> miles)
              case _ => Map()
            }
          }
          case _ => Map()
        }
      }
      case _ => Map()
    }
  }

  def extractData(el: Element): Map[String, String] = {
    extractAuctionInfo(el) ++
      extractBidStatus(el) ++
      extractCarType(el) ++
      extractPrice(el) ++
      extractLotBreakdown(el) ++
      extractMiles(el)
  }

  def dataFromHrefs(hrefs: List[String], baseURL: String): List[Map[String, String]] =
    hrefs match {
      case List() => List()
      case href :: rest => {
        val linkToCar: String = baseURL + href
        val carLinkDoc: Element = mecumDao.connect(linkToCar, res.cookies()).get().body()
        val carDataMap: Map[String, String] = extractData(carLinkDoc) ++ Map("Link" -> linkToCar)
        carDataMap :: dataFromHrefs(rest, baseURL)
      }
    }
}
