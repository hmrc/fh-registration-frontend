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

  val firstNameKey = "firstName"
  val lastNameKey = "lastName"
  val hasNationalInsuranceNumberKey = "hasNationalInsuranceNumber"
  val nationalInsuranceNumberKey = "nationalInsuranceNumber"
  val hasPassportNumberKey = "hasPassportNumber"
  val passportNumberKey = "passportNumber"
  val nationalIDKey = "nationalID"
  val companyNameKey = "companyName"
  val vatRegistrationKey = "vatRegistration"
  val companyRegistrationKey = "companyRegistration"
  val roleKey = "role"

  val identificationTypeKey = "identificationType"
  val companyIdentificationKey = "companyIdentification"
  val individualIdentificationKey = "individualIdentification"

  val companyOfficersKey = "companyOfficers"

  val hasNinoMapping = hasNationalInsuranceNumberKey → yesOrNo
  val ninoMapping = nationalInsuranceNumberKey → (nino onlyWhen (hasNinoMapping is true withPrefix individualIdentificationKey))
  val hasPassportNumberMapping = hasPassportNumberKey → (yesOrNo onlyWhen (hasNinoMapping is false withPrefix individualIdentificationKey))
  val passportNumberMapping = passportNumberKey → (passportNumber onlyWhen (hasPassportNumberMapping is Some(true) withPrefix individualIdentificationKey))
  val nationalIdMapping = "nationalID" → (nationalIdNumber onlyWhen (hasPassportNumberMapping is Some(false) withPrefix individualIdentificationKey) )

  val roles = List("Director", "Company Secretary", "Director and Company Secretary", "Member")

  val companyOfficerTypeMapping = identificationTypeKey → enum(CompanyOfficerType)

  val companyOfficerIndividualMapping = mapping(
    firstNameKey → personName,
    lastNameKey → personName,
    hasNinoMapping,
    ninoMapping,
    hasPassportNumberMapping,
    passportNumberMapping,
    nationalIdMapping,
    roleKey → oneOf(roles)
  )(CompanyOfficerIndividual.apply)(CompanyOfficerIndividual.unapply)

  val hasVatMapping = "hasVat" → yesOrNo

  val companyOfficerCompanyMapping = mapping(
    companyNameKey → nonEmptyText,
    hasVatMapping,
    "vatRegistration" → (vatRegistrationNumber onlyWhen (hasVatMapping is true withPrefix companyIdentificationKey)),
    "companyRegistration" → (companyRegistrationNumber onlyWhen (hasVatMapping is false withPrefix companyIdentificationKey)),
    "role" → oneOf(roles)
  )(CompanyOfficerCompany.apply)(CompanyOfficerCompany.unapply)

  val companyOfficerMapping: Mapping[CompanyOfficer] = mapping(
    companyOfficerTypeMapping,
    companyIdentificationKey → (companyOfficerCompanyMapping onlyWhen (companyOfficerTypeMapping is CompanyOfficerType.Company)),
    individualIdentificationKey → (companyOfficerIndividualMapping onlyWhen (companyOfficerTypeMapping is CompanyOfficerType.Individual))
  ) {
    case (identificationType, company, individual) ⇒
      CompanyOfficer(
        identificationType,
        company getOrElse individual.get
      )
  } {
    case CompanyOfficer(identificationType, identification) ⇒ identification match {
      case i: CompanyOfficerIndividual ⇒ Some((identificationType, None, Some(i)))
      case c: CompanyOfficerCompany ⇒ Some((identificationType, Some(c), None))
    }
  }

  val companyOfficerForm = Form(companyOfficerMapping)

  val companyOfficersForm = Form(
    mapping(
      companyOfficersKey → list(companyOfficerMapping)
    )(CompanyOfficers.apply)(CompanyOfficers.unapply)
  )
}
