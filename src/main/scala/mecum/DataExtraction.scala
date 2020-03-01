package mecum

import org.jsoup.nodes.Element

class DataExtractionImpl() {

  def convertToTimeStamp(date: String): String = {
    val regex = raw"^([\d]+) (\w+) (\d+)-(\d+)".r

    val monthToIntMap = Map(
      "Jan" -> 1, "Feb" -> 2,
      "Mar" -> 3, "Apr" -> 4,
      "May" -> 5, "June" -> 6,
      "Jul" -> 7, "Aug" -> 8,
      "Sep" -> 9, "Oct" -> 10,
      "Nov" -> 11, "Dec" -> 12
    )

    val formatter = "%04d-%02d-%02dT00:00:00Z"

    date match {
      case regex(year, month, startDay, endDay) => {
        val startDate = formatter.format(year.toInt, monthToIntMap(month), startDay.toInt)
        startDate
      }
      case _ => ""
    }
  }

  def extractAuctionInfo(el: Element): Map[String, String] = {
    val lotAuctionInfo = Option(el.selectFirst("h3.lot-auction-info"))
    val elRegex = raw"(Lot [^ ]+) ([^0-9]+) (.*)".r

    lotAuctionInfo match {
      case Some(auctionInfoEl) => auctionInfoEl.text match {
        case elRegex(lot, location, date) => {
          val convertedDate = convertToTimeStamp(date)
          Map(
            "lot" -> lot,
            "auctionLocation" -> location,
            "auctionDate" -> convertedDate)
        }
        case _ => Map("lot" -> "",
          "auctionLocation" -> "",
          "auctionDate" -> "")
      }
      case _ => Map("lot" -> "",
        "auctionLocation" -> "",
        "auctionDate" -> "")
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
              case Some(status) => Map("auctionResult" -> status)
              case _ => Map("auctionResult" -> "")
            }
          }
          case _ => Map("auctionResult" -> "")
        }
      }
      case _ => Map("auctionResult" -> "")
    }
  }

  def extractCarType(el: Element): Map[String, String] = {
    val lot = Option(el.selectFirst("h1.lot-title"))
    val elRegex = """([\d]+) (.*)""".r

    lot match {
      case Some(lotEl) => {
        lotEl.text() match {
          case elRegex(year, car) => Map("makeModel" -> car, "year" -> year)
          case _ => Map("makeModel" -> "", "year" -> "")
        }
      }
      case _ => Map("makeModel" -> "", "year" -> "")
    }
  }

  def extractPrice(el: Element): Map[String, String] = {
    val price: Option[Element] = Option(el.selectFirst("span.lot-price"))

    price match {
      case Some(priceEl) => Map("price" -> priceEl.text().replaceAll("\\D", ""))
      case _ => Map("price" -> "")
    }
  }

  private def genMap(carComponents: List[String]): Map[String, String] = {
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
      case Some(lotBreakdown) => {
        val children = lotBreakdown.children()
        val lotInfo = children.toString.split("\n").toList
        genMap(lotInfo)
      }
      case _ => Map("Engine" -> "", "Trans" -> "", "Color" -> "", "Interior" -> "")
    }

  }

  def extractMiles(el: Element): Map[String, String] = {
    val milesEl = Option(el.selectFirst("ul.lot-highlights"))
    val milesLiRegex = """.*[<li>| ]([0-9,]+).*miles.*""".r

    milesEl match {
      case Some(highlights) => {
        val highlightList: List[String] = highlights.children().toString.split("\n").toList
        val milesLi = highlightList.filter(_.contains("miles")).lift(0)
        milesLi match {
          case Some(milesLiEl) => {
            milesLiEl match {
              case milesLiRegex(miles) => Map("miles" -> miles.replaceAll("\\D", ""))
              case _ => Map("miles" -> "")
            }
          }
          case _ => Map("miles" -> "")
        }
      }
      case _ => Map("miles" -> "")
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
}
