package mecum

import mecum.App.{linkToCar, mecumDao, res}
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class DataExtractionImpl {

  // will be private
  // separate each step to different functions
  def extractData(el: Element): String = {
    val auctionInfo: String = el.select("h3.lot-auction-info").get(0).text()
    println(s"AUCTION INFO: ${auctionInfo}")
    val bidStatus: String = el.select("div.auction-results").get(0).attributes().toString()
    println(s"BID STATUS: ${bidStatus}")
    val yearMakeModel: String = el.select("h1.lot-title").get(0).text()
    println(s"YEARMAKEMODEL: ${yearMakeModel}")
    val lotPrice: String = el.select("span.lot-price").get(0).text()
    println(s"LOT PRICE: ${lotPrice}")
    val etci: Elements = el.select(".lot-breakdown-list").get(0).children()
    println(etci)
    val miles: Elements = el.select("ul.lot-highlights").get(0).children()
    val filtered = miles.toArray.toList
    println(filtered.filter((str) => {
      str.toString().contains("miles")
    }))
    ""
  }

  // want to return List[CarMeta]
  def dataFromHrefs(hrefs: List[String], baseURL: String): List[String] =
    hrefs match {
      case List() => List()
      case href :: rest => {
        val linkToCar: String = baseURL + href
        val carLinkDoc: Element = mecumDao.connect(linkToCar, res.cookies()).get().body()
        val str = carLinkDoc.select("h1.lot-title").get(0).text()
        println("------------------------")
        println(str)
        // call extract data
        str :: dataFromHrefs(rest, baseURL)
      }
    }

}
