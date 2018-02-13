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

import play.api.data.Forms.{list, mapping, nonEmptyText, optional}
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{CompanyOfficer, CompanyOfficerCompany, CompanyOfficerIndividual, CompanyOfficers}

object CompanyOfficersForm {

  val companyOfficerIndividualMapping = mapping(
    "firstName" → nonEmptyText,
    "lastName" → nonEmptyText,
    "nationalInsuranceNumber" → optional(nonEmptyText),
    "passportNumber" → optional(nonEmptyText),
    "nationalID" → optional(nonEmptyText),
    "role" → nonEmptyText
  )(CompanyOfficerIndividual.apply)(CompanyOfficerIndividual.unapply)

  val companyOfficerCompanyMapping = mapping(
    "companyName" → nonEmptyText,
    "vatRegistration" → optional(nonEmptyText),
    "companyRegistration" → optional(nonEmptyText),
    "role" → nonEmptyText
  )(CompanyOfficerCompany.apply)(CompanyOfficerCompany.unapply)

  val companyOfficerMapping: Mapping[CompanyOfficer] = mapping(
    "identificationType" → nonEmptyText,
    "companyIdentification" → optional(companyOfficerCompanyMapping),
    "individualIdentification" → optional(companyOfficerIndividualMapping)
  ) {
    case (identificationType, company, individual) ⇒
      CompanyOfficer(
        identificationType,
        company.getOrElse(
          individual.getOrElse(
            CompanyOfficerIndividual("first","lastName",Some(""),Some(""),Some(""),"role")
          )
        )
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
      "companyOfficers" → list(companyOfficerMapping)
    )(CompanyOfficers.apply)(CompanyOfficers.unapply)
  )
}
