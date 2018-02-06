package uk.gov.hmrc.fhregistrationfrontend.forms.definitions


import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.TradingName

object TradingNameForm {

  val tradingNameForm = Form(
    mapping(
      "value" → optional(nonEmptyText) //TODO regex and just in case the answer is yes
    )(TradingName.apply)(TradingName.unapply)
  )
}
