package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, of, optional}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Constraints.oneOf
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.address
import uk.gov.hmrc.fhregistrationfrontend.forms.models.MainBusinessAddress
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters.radioButton

object MainBusinessAddressForm {

  val mainBusinessAddress = Form(
    mapping(
      "timeAtCurrentAddress" → (nonEmptyText verifying oneOf(MainBusinessAddress.TimeAtCurrentAddressOptions)),
      "previousAddress.yesNo" → of(radioButton),
      "previousAddress.value" → optional(address) //TODO check for yes/no
    )(MainBusinessAddress.apply)(MainBusinessAddress.unapply)
  )
}

