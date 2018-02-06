package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{CompanyRegistrationNumber, ContactPerson}
import play.api.data.Forms.{mapping, nonEmptyText, of, optional}
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters._
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.address
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.internationalAddress


class CompanyRegistrationNumberForm {

  //TODO use regex
  val companyRegistrationNumberForm = Form(
    mapping(
      "crn" → nonEmptyText
    )(CompanyRegistrationNumber.apply)(CompanyRegistrationNumber.unapply)
  )

}
