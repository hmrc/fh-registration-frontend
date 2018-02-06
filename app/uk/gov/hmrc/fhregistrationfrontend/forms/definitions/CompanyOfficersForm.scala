package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Forms.{list, mapping, nonEmptyText, optional}
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object CompanyOfficersForm {

  val companyOfficersForm = Form(
    mapping(
      "value" → list(companyOfficerMapping)
    )(CompanyOfficers.apply)(CompanyOfficers.unapply)
  )

  val companyOfficerMapping: Mapping[CompanyOfficer] = mapping(
    "identificationType" → nonEmptyText,
    "companyIdentification" → optional(companyOfficerCompanyMapping),
    "individualIdentification" → optional(companyOfficerIndividualMapping)
  ) {
    case (identificationType, company, individual) ⇒
      CompanyOfficer(identificationType, company getOrElse individual.get)
  } {
    case CompanyOfficer(identificationType, identification) ⇒ identification match {
      case i: CompanyOfficerIndividual ⇒ Some((identificationType, None, Some(i)))
      case c: CompanyOfficerCompany ⇒ Some((identificationType, Some(c), None))
    }
  }

  val companyOfficerIndividualMapping = mapping(
    "firstName" → nonEmptyText,
    "lastName" → nonEmptyText,
    "nino" → optional(nonEmptyText),
    "passport" → optional(nonEmptyText),
    "nationalId" → optional(nonEmptyText),
    "role" → optional(nonEmptyText)
  )(CompanyOfficerIndividual.apply)(CompanyOfficerIndividual.unapply)

  val companyOfficerCompanyMapping = mapping(
    "companyName" → nonEmptyText,
    "vat" → optional(nonEmptyText),
    "crn" → optional(nonEmptyText),
    "role" → nonEmptyText
  )(CompanyOfficerCompany.apply)(CompanyOfficerCompany.unapply)

}
