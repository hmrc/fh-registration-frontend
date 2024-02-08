package uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$

import play.api.libs.json.JsPath
import pages.QuestionPage

case class $className$Page(index: Int) extends QuestionPage[String] {

  override def path: JsPath = JsPath \ "$packageName$" \ index.toString \ toString

  override def toString: String = "$className;format="decap"$"
}
