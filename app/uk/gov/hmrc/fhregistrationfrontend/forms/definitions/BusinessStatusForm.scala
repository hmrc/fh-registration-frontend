package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import play.api.data.Forms.{list, mapping, nonEmptyText, optional, of}
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters.radioButton
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.localDate

object BusinessStatusForm {

  val businessStatusForm = Form(
    mapping(
      "isNewFulfimentBusiness" → of(radioButton),
      "proposedStartDate" → optional(localDate)
    )(BusinessStatus.apply)(BusinessStatus.unapply)
  )
}
