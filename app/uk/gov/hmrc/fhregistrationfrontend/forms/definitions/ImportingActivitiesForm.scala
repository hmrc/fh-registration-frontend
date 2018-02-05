package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import play.api.data.Forms.{list, mapping, nonEmptyText, optional, of}
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters.radioButton
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.localDate

object ImportingActivitiesForm {

  val importingActivitiesForm = Form(
    mapping(
      "hasEori" → of(radioButton),
      "eoriNumber" → optional(eoriNumberMapping)
    )(ImportingActivities.apply)(ImportingActivities.unapply)
  )

  val eoriNumberMapping = mapping(
    "eoriNumber" → nonEmptyText,
    "goodsImportedOutsideEori" → of(radioButton)
  )(EoriNumber.apply)(EoriNumber.unapply)

}
