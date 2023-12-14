package uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners

import pages.QuestionPage
import play.api.libs.json.JsPath
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnersEnterAddress

case class EnterAddressPage(index: Int) extends QuestionPage[BusinessPartnersEnterAddress] {
  override def path: JsPath = JsPath \ "businessPartners" \ index.toString \ toString

  override def toString: String = "enterAddress"
}
