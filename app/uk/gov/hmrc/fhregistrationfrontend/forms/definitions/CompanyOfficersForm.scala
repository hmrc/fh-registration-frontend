/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Forms.{list, mapping, nonEmptyText}
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings._
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl.MappingsApi.{MappingOps, MappingWithKeyOps}
import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object CompanyOfficersForm {

  val hasNinoMapping = "hasNationalInsuranceNumber" → yesOrNo
  val ninoMapping = "nationalInsuranceNumber" → (nino onlyWhen (hasNinoMapping is true withPrefix "individualIdentification"))
  val hasPassportNumberMapping = "hasPassportNumber" → (yesOrNo onlyWhen (hasNinoMapping is false withPrefix "individualIdentification"))
  val passportNumberMapping = "passportNumber" → (passportNumber onlyWhen (hasPassportNumberMapping is Some(true) withPrefix "individualIdentification"))
  val nationalIdMapping = "nationalID" → (nationalIdNumber onlyWhen (hasPassportNumberMapping is Some(false) withPrefix "individualIdentification") )

  val roles = List("Director", "Company Secretary", "Director and Company Secretary", "Member")

  val companyOfficerTypeMappig = "identificationType" → enum(CompanyOfficerType)


  val companyOfficerIndividualMapping = mapping(
    "firstName" → personName,
    "lastName" → personName,
    hasNinoMapping,
    ninoMapping,
    hasPassportNumberMapping,
    passportNumberMapping,
    nationalIdMapping,
    "role" → oneOf(roles)
  )(CompanyOfficerIndividual.apply)(CompanyOfficerIndividual.unapply)

  val hasVatMapping = "hasVat" → yesOrNo

  val companyOfficerCompanyMapping = mapping(
    "companyName" → nonEmptyText,
    hasVatMapping,
    "vatRegistration" → (vatRegistrationNumber onlyWhen (hasVatMapping is true withPrefix "companyIdentification")),
    "companyRegistration" → (companyRegistrationNumber onlyWhen (hasVatMapping is false withPrefix "companyIdentification")),
    "role" → oneOf(roles)
  )(CompanyOfficerCompany.apply)(CompanyOfficerCompany.unapply)

  val companyOfficerMapping: Mapping[CompanyOfficer] = mapping(
    companyOfficerTypeMappig,
    "companyIdentification" → (companyOfficerCompanyMapping onlyWhen (companyOfficerTypeMappig is CompanyOfficerType.Company)),
    "individualIdentification" → (companyOfficerIndividualMapping onlyWhen (companyOfficerTypeMappig is CompanyOfficerType.Individual))
  ) {
    case (identificationType, company, individual) ⇒
      CompanyOfficer(identificationType, company getOrElse individual.get)
  } {
    case CompanyOfficer(identificationType, identification) ⇒ identification match {
      case i: CompanyOfficerIndividual ⇒ Some((identificationType, None, Some(i)))
      case c: CompanyOfficerCompany ⇒ Some((identificationType, Some(c), None))
    }
  }

  val companyOfficerForm = Form(companyOfficerMapping)

  val companyOfficersForm = Form(
    mapping(
      "companyOfficers" → list(companyOfficerMapping)
    )(CompanyOfficers.apply)(CompanyOfficers.unapply)
  )
}
