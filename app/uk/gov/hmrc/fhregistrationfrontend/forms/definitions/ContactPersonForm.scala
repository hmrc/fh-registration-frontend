package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ContactPerson
import play.api.data.Forms.{mapping, nonEmptyText, of, optional}
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters._
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.address
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.internationalAddress

object ContactPersonForm {

  val contactPersonForm = Form(
    mapping(
      "firstName" → nonEmptyText,
      "lastName" → nonEmptyText,
      "jobTitle" → nonEmptyText,
      "telephone" → nonEmptyText,
      "emailAddress" → nonEmptyText,
      "hasOtherContactAddress" → of(radioButton),
      "ukOtherAddress" → optional(of(radioButton)),
      "otherContactAddress" → optional(address),
      "otherContactInternationalAddress" → optional(internationalAddress)
    )(ContactPerson.apply)(ContactPerson.unapply)
  )

}
