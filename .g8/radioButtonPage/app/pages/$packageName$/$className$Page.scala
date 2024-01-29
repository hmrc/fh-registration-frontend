package pages.$packageName$

import models.$packageName$.$className$
import play.api.libs.json.JsPath
import pages.QuestionPage

case object $className$Page extends QuestionPage[$className$] {

  override def path: JsPath = JsPath \ journeyType \ toString

  def journeyType: String = "$packageName$"
  override def toString: String = "$className;format="decap"$"
}
