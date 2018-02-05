package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{TradingName, VatNumber}


object VatNumberForm {

  val vatNumberForm = Form(
    mapping(
      "value" → optional(nonEmptyText)
    )(VatNumber.apply)(VatNumber.unapply)
  )
}
