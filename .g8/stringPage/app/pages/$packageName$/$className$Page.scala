package pages.$packageName$

import play.api.libs.json.JsPath
import pages.QuestionPage

case object $className$Page extends QuestionPage[String] {

  override def path: JsPath = JsPath \ journeyType \ toString

  def journeyType: String = "$packageName$"
  override def toString: String = "$className;format="decap"$"
}
