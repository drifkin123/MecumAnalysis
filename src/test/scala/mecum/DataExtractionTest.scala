package mecum

import scala.collection._
import org.scalatest.Assertions
import org.junit.Test
import org.jsoup.nodes.{Attributes, Element}

class DataExtractionTest extends Assertions {

  val uut = new DataExtractionImpl()

  class ElementStub(tag: String, retText: String) extends Element(tag) {
    override def selectFirst(cssQuery: String): Element = {
      ElementStub(retText)
    }
    override def text(): String = {
      retText
    }

    override def attributes(): Attributes = {
      new Attributes().put("class", "sold")
    }
  }
  object ElementStub {
    def apply(retText: String): ElementStub = {
      new ElementStub("something", retText)
    }
  }

  @Test def shouldExtractAuctionInfo(): Unit = {
    val expected: Map[String, String] = Map(
      "lot" -> "Lot E92",
      "auctionLocation" -> "Kissimmee",
      "auctionDate" -> "2020 Jan 2-12"
    )

    val retText: String = "Lot E92 Kissimmee 2020 Jan 2-12"
    val el: ElementStub =  ElementStub(retText)

    val actual = uut.extractAuctionInfo(el)
    assert(expected.toSet == actual.toSet)
  }

  @Test def shouldExtractBidStatus(): Unit = {
    val expected: Map[String, String] = Map(
      "auctionResult" -> "sold"
    )

    val retText: String =  """class="auction-results sold has-price""""
    val el: ElementStub = ElementStub(retText)

    val actual = uut.extractBidStatus(el)
    assert(expected.toSet == actual.toSet)
  }

  @Test def shouldExtractCarType(): Unit = {
    val expected = Map(
      "makeModel" -> "Ferrari 458 Italia",
      "year" -> "2014"
    )

    val retText: String = "2014 Ferrari 458 Italia"
    val el: ElementStub = ElementStub(retText)

    val actual = uut.extractCarType(el)
    assert(expected.toSet == actual.toSet)
  }

  @Test def shouldExtractPrice(): Unit = {
    val expected = Map(
      "price" -> "5000"
    )

    val retText: String = "$5,000"
    val el = ElementStub(retText)

    val actual = uut.extractPrice(el)
    assert(expected.toSet == actual.toSet)
  }

  @Test def shouldExtractLotBreakdown(): Unit = {
    val expected = Map(
      "Engine" -> "3.5L",
      "Trans" -> "Automatic",
      "Color" -> "Gray",
      "Interior" -> "Beige"
    )

    val el = new Element("ul").
      attr("class", "lot-breakdown-list").
      append("<li><h5>Engine</h5>3.5L</li> \n" +
        " <li><h5>Trans</h5>Automatic</li> \n" +
        " <li><h5>Color</h5>Gray</li> \n" +
        " <li><h5>Interior</h5>Beige</li>")

    val actual = uut.extractLotBreakdown(el)
    assert(expected.toSet == actual.toSet)
  }

  @Test def shouldExtractMiles1(): Unit = {
    val expected = Map(
      "miles" -> "8200"
    )

    val el = new Element("ul").
      attr("class", "lot-highlights").
      append("<li>8 passenger seating</li> \n" +
        " <li>Leather seats</li> \n" +
        " <li>Cruise control</li> \n" +
        " <li>Dual air conditioning</li> \n" +
        " <li>Full roof rack</li> \n" +
        " <li>Panoramic sunroof</li> \n" +
        " <li>Automatic liftgate</li> \n" +
        " <li>Power hideaway mirrors</li> \n" +
        " <li>Power hideaway seats</li> \n" +
        " <li>Blind spot detector</li> \n" +
        " <li>Navigation</li> \n" +
        " <li>Audio steering controls</li> \n" +
        " <li>Engine auto shut off with cancellation option</li> \n" +
        " <li>Blue tooth capability</li> \n" +
        " <li>Back-up camera</li> \n" +
        " <li>Keyless entry</li> \n" +
        " <li>Running boards</li> \n" +
        " <li>Tow package</li> \n" +
        " <li>8,200 actual miles</li> ")

    val actual = uut.extractMiles(el)
    assert(expected.toSet == actual.toSet)
  }
  @Test def shouldExtractMiles2(): Unit = {
    val expected = Map(
      "miles" -> "8200"
    )

    val el = new Element("ul").
      attr("class", "lot-highlights").
      append("<li>8 passenger seating</li> \n" +
        " <li>Leather seats</li> \n" +
        " <li>With 8,200 actual miles</li> ")

    val actual = uut.extractMiles(el)
    assert(expected.toSet == actual.toSet)
  }
}
