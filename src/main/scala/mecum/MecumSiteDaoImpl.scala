package mecum

import java.util

import mecum.App.mecumDao
import org.jsoup.Connection.Response
import org.jsoup.{Connection, Jsoup}
import org.jsoup.nodes.{Document, Element, FormElement}
import org.jsoup.select.Elements

import collection.JavaConverters._

class MecumSiteDaoImpl(
                        val referrer: String,
                        val userAgent: String,
                        val searchPageURL: String,
                        val baseURL: String,
                        val loginURL: String) {

  def login(creds: List[(String, String)]): Response = {
    val conn: Connection = mecumDao.connect(mecumDao.loginURL)
    val doc: Document = mecumDao.getDocument(conn)
    val form: FormElement = mecumDao.findForm("infonet-login", doc)
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

  def hrefOfNextPage(doc: Document): String = {
    val el: Element = doc.body.select("[rel=next]").first()
    el.attr("href")
  }

}
