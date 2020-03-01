package mecum

import java.util

import org.jsoup.Connection.Response
import org.jsoup.{Connection, Jsoup}
import org.jsoup.nodes.{Document, Element, FormElement}
import org.jsoup.select.Elements

import collection.JavaConverters._

class MecumSiteDaoImpl() {

  val referrer = "https://www.mecum.com/infonet/search/reset/1/"
  val userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) " +
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"
  val loginURL: String = "https://www.mecum.com/infonet/login/"
  val searchPageURL: String = "https://www.mecum.com/infonet/search/reset/1"
  val baseURL: String = "https://www.mecum.com"

  def login(creds: List[(String, String)]): Response = {
    val conn: Connection = connect(loginURL)
    val doc: Document = getDocument(conn)
    val form: FormElement = findForm("infonet-login", doc)
    submitForm(form, creds)
  }

  def requestBodyForMake: (String, String) => String =
    (searchScope: String, make: String) =>
      s"searchScope=${searchScope}&searchText=&searchMake=$make&searchModel=&searchYearStart=&searchYearEnd=&submit="

  def connect(url: String): Connection = {
    org.jsoup.Jsoup.connect(url)
  }

  def connect(url: String, cookies: util.Map[String, String]): Connection = {
    org.jsoup.Jsoup.connect(url).
      cookies(cookies).
      referrer(referrer).
      userAgent(userAgent)
  }

  def getDocument(conn: Connection): Document = {
    conn.get()
  }

  def findForm(formName: String, doc: Document): FormElement = {
    doc.body.select(s"[name=$formName]").forms().get(0)
  }

  def submitForm(form: FormElement, data: List[(String, String)]): Connection.Response = {
    val dataMap: Map[String, String] = data.toMap
    form.submit().data(dataMap.asJava).execute()
  }

  def submitSearchForm(cookies: util.Map[String, String], searchScope: String, make: String): Document = {
    Jsoup.connect(searchPageURL).
      cookies(cookies).
      referrer(referrer).
      userAgent(userAgent).
      requestBody(requestBodyForMake(searchScope, make)).
      post()
  }

  def hrefsForAllCarsOnPage(doc: Document): List[String] = {
    val carLinks: Elements = doc.body.select("a.lot-title")
    val carLinkHrefList: Array[String] = for (link <- carLinks.toArray()) yield {
      val linkElement: Element = link.asInstanceOf[Element]
      linkElement.attr("href")
    }
    carLinkHrefList.toList
  }

  def hrefOfNextPage(doc: Document): Option[String] = {
    val el: Option[Element] = Option(doc.body.select("[rel=next]").first())

    el match {
      case Some(next) => Some(next.attr("href"))
      case _ => None
    }
  }
}
