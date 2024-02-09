package uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import play.api.libs.json.JsPath
import pages.QuestionPage

case class $className$Page(index: Int) extends QuestionPage[$className$.Value] {

  override def path: JsPath = JsPath \ "$packageName$" \ index.toString \ toString

  override def toString: String = "$className;format="decap"$"
}
