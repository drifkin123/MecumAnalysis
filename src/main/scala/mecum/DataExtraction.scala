package mecum

import mecum.App.{linkToCar, mecumDao, res}
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class DataExtractionImpl {

  def extractAuctionInfo(el: Element): Option[(String, String, String)] = {
    el.selectFirst("h3.lot-auction-info") match {
      case el: Element => {
        val elRegex = raw"(Lot [^ ]+) ([^0-9]+) (.*)".r
        el.text match {
          case elRegex(lot, location, date) => Some((lot, location, date))
          case _ => None
        }
      }
      case _ => None
    }
  }

  def extractBidStatus(el: Element): Option[String] = {
    val options: List[String] = List("bid-goes-on", "sold", "high-bid")

    el.selectFirst("div.auction-results") match {
      case attrs => {
        val attributes: String = attrs.attributes().toString.trim
        val elRegex = """class="(.*)"""".r
        attributes match {
          case elRegex(results) =>
            results.split(" ").filter(options.contains(_)).toList.lift(0)
          case _ => None
        }
      }
      case _ => None
    }
  }

  def extractCarType(el: Element): Option[(String, String)] = {
    val lot = el.selectFirst("h1.lot-title").text
    val elRegex = """([\d]+) (.*)""".r

    lot match {
      case elRegex(year, car) => Some((year, car))
      case _ => None
    }
  }

  def extractData(el: Element): String = {

    println(extractAuctionInfo(el))
    println(extractBidStatus(el))
    println(extractCarType(el))
//    val auctionInfo: String = el.selectFirst("h3.lot-auction-info").text()
//    println(s"AUCTION INFO: ${auctionInfo}")
//    val bidStatus: String = el.selectFirst("div.auction-results").attributes().toString()
//    println(s"BID STATUS: ${bidStatus}")
//    val yearMakeModel: String = el.selectFirst("h1.lot-title").text()
//    println(s"YEARMAKEMODEL: ${yearMakeModel}")
//    val lotPrice: String = el.selectFirst("span.lot-price").text()
//    println(s"LOT PRICE: ${lotPrice}")
//    val etci: Elements = el.selectFirst(".lot-breakdown-list").children()
//    println(etci)
//    val miles: Elements = el.selectFirst("ul.lot-highlights").children()
//    val filtered = miles.toArray.toList
//    println(filtered.filter((str) => {
//      str.toString().contains("miles")
//    }))
    ""
  }

  // want to return List[CarMeta]
  def dataFromHrefs(hrefs: List[String], baseURL: String): List[String] =
    hrefs match {
      case List() => List()
      case href :: rest => {
        val linkToCar: String = baseURL + href
        val carLinkDoc: Element = mecumDao.connect(linkToCar, res.cookies()).get().body()
        val str = carLinkDoc.selectFirst("h1.lot-title").text()
        println("------------------------")
        println(str)
        // call extract data
        str :: dataFromHrefs(rest, baseURL)
      }
    }

}
