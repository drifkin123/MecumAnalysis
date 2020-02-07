package mecum

import java.util

import org.jsoup.{Connection, Jsoup}
import org.jsoup.Connection.Response
import org.jsoup.nodes.{Document, FormElement}

trait MecumSiteDao {
  def connect(): Connection
  def getDocument(conn: Connection): Document
  def findForm(formName: String, doc: Document): FormElement
  def submitForm(form: FormElement, date: List[(String, String)]): Response
  def documentFromSubmitForm(searchPageURL: String, cookies: util.Map[String, String], referrer: String,
                             userAgent: String, requestBody: String): Document
}
