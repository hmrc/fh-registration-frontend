package uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners

import pages.QuestionPage
import play.api.libs.json.JsPath
import uk.gov.hmrc.fhregistrationfrontend.forms.models.PartnerName

case class IndividualsAndSoleProprietorsPartnerNamePage(index: Int) extends QuestionPage[PartnerName] {

  override def path: JsPath = JsPath \ "businessPartners" \ index.toString \ toString

  override def toString: String = "individualsAndSoleProprietorsPartnerName"
}
