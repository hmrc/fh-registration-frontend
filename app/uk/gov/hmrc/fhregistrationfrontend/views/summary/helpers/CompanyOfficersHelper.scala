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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{CompanyOfficer, ListWithTrackedChanges}
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.{Helpers, SummaryRowParams}
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.{ActionItem, Actions, Text}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{CompanyOfficerCompany, CompanyOfficerIndividual}

object CompanyOfficersHelper {

  def apply(companyOfficers: ListWithTrackedChanges[CompanyOfficer], mode: Mode, lastUpdateTimestamp: String)(implicit
    messages: Messages
  ) =
    companyOfficers.values.zipWithIndex.flatMap { case (companyOfficer, index) =>
      val individualOrCompanyDetails = companyOfficer.identification match {
        case individual: CompanyOfficerIndividual =>
          CompanyOrIndividualHelper.createIndividual(individual)(using messages: Messages)

        case company: CompanyOfficerCompany =>
          CompanyOrIndividualHelper.createCompany(company)(using messages: Messages)
      }

      val isEditable = Mode `isEditable` mode

      def getActions(index: Int) =
        if (companyOfficers.values.size > 1) {
          Seq(
            ActionItem(
              href = s"form/companyOfficers/$index/confirmDelete/$lastUpdateTimestamp",
              content = Text("Remove"),
              visuallyHiddenText = Some(messages("fh.company_officers.each.title", index))
            ),
            ActionItem(
              href = s"form/companyOfficers/$index",
              content = Text("Change"),
              visuallyHiddenText = Some(messages("fh.company_officers.each.title", index))
            )
          )
        } else {
          Seq(
            ActionItem(
              href = s"form/companyOfficers/$index",
              content = Text("Change"),
              visuallyHiddenText = Some(messages("fh.company_officers.each.title", index))
            )
          )
        }

      val officerLabel = Helpers.createSummaryRow(
        SummaryRowParams(
          Some(Messages("fh.company_officers.each.title", index + 1)),
          None,
          None,
          GroupRow.Single
        ),
        summaryActions = if (isEditable) {
          Some(Actions(items = getActions(index + 1)))
        } else { None }
      )

      officerLabel +: individualOrCompanyDetails
    }.toSeq
}
