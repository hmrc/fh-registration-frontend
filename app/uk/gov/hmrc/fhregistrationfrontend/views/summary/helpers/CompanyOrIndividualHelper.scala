/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.views.summary.helpers

import play.api.i18n.Messages
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.{Helpers, SummaryRowParams}
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.SummaryListRow
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{CompanyOfficerCompany, CompanyOfficerIndividual}

object CompanyOrIndividualHelper {

  def createIndividual(individual: CompanyOfficerIndividual)(implicit messages: Messages): Seq[SummaryListRow] = {

    val titleRow = {
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.summary.companyOfficerType")),
          messages("fh.company_officers.individual.label"),
          None,
          GroupRow.Member
        ),
        None)
    }

    val descriptiveRow = {
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.generic.name")),
          individual.firstName + " " + individual.lastName,
          None,
          GroupRow.Member
        ),
        None)
    }

    val Role = {
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.summary.companyOfficerRole")),
          individual.role,
          None,
          GroupRow.Bottom
        ),
        None)
    }

    val conditionalHtml = {
      if (individual.hasNino) {
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(messages("fh.company_officers.individual.nino.label")),
            individual.nino,
            None
          ),
          None)
      } else {
        if (individual.hasPassportNumber.contains(true)) {
          Helpers.createSummaryRow(
            SummaryRowParams(
              Some(messages("fh.individualIdentification.passportNumber.label")),
              individual.passport,
              None
            ),
            None)
        } else {
          Helpers.createSummaryRow(
            SummaryRowParams(
              Some(messages("fh.company_officers.individual.nationalID.label")),
              individual.nationalId,
              None
            ),
            None)
        }
      }
    }

    Seq(titleRow, conditionalHtml, descriptiveRow, Role)
  }

  def createCompany(company: CompanyOfficerCompany)(implicit messages: Messages) = {

    val titleRow = {
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.summary.companyOfficerType")),
          Messages("fh.company_officers.company.label"),
          None,
          GroupRow.Member
        ),
        None)
    }

    val descriptiveRow = {
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.company_officers.company.company_name.label")),
          company.companyName,
          None,
          GroupRow.Member
        ),
        None)
    }

    val companyRole = {
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.summary.companyOfficerRole")),
          company.role,
          None,
          GroupRow.Bottom
        ),
        None)
    }

    val vatNumber = {
      Helpers.createSummaryRow(
        SummaryRowParams(
          Some(Messages("fh.vatNumber.label")),
          company.vat,
          None
        ),
        None)
    }

    val noVatNumber = {
      Helpers.createSummaryRow(
        SummaryRowParams.ofBoolean(
          Some(Messages("fh.company_officers.company.vat_registration.confirm.label")),
          company.hasVat,
          None,
          GroupRow.Member
        ),
        None)
    }

    val CompanyRegistrationNumber = {
      Helpers.createSummaryRow(
        SummaryRowParams(
          Some(Messages("fh.company_officers.company.company_registration_number.label")),
          company.crn,
          None
        ),
        None)
    }

    val conditionalHtml = {
      if (company.hasVat) {
        Seq(titleRow, vatNumber, descriptiveRow, companyRole)
      } else {
        Seq(titleRow, noVatNumber, CompanyRegistrationNumber, descriptiveRow, companyRole)
      }
    }

    conditionalHtml
  }

}
