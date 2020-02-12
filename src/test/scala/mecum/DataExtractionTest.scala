package mecum

import scala.collection._
import org.scalatest.Assertions
import org.junit.Test
import org.jsoup.nodes.Element

class DataExtractionTest extends Assertions {

  val dataExtractor = new DataExtractionImpl()

  class ElementStub(tag: String, retText: String) extends Element(tag) {
    override def selectFirst(cssQuery: String): Element = {
      new ElementStub("something", retText)
    }
    override def text(): String = {
      retText
    }
  }

  @Test def shouldExtractAuctionInfo(): Unit = {

    val expected: Map[String, String] = Map(
      "lot" -> "Lot J30",
      "auctionLocation" -> "New Hampshire",
      "auctionDate" -> "2020"
    )

    val tag: String = "div"
    val retText: String = "Lot J30 New Hampshire 2020"
    val el: ElementStub = new ElementStub(tag, retText)

    val actual = dataExtractor.extractAuctionInfo(el)
    assert((expected.toSet diff actual.toSet).size == 0)
  }





}
