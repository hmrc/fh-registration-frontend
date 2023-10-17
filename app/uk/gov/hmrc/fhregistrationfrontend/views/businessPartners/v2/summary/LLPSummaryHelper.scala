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

package uk.gov.hmrc.fhregistrationfrontend.views.businessPartners.v2.summary

import play.api.i18n.Messages
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessPartnerIndividual => BusinessPartnerIndividualModel}
import uk.gov.hmrc.fhregistrationfrontend.views.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.{SummaryListRow, Text}

object LLPSummaryHelper {
  def apply(individual: BusinessPartnerIndividualModel)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.businessPartners.summary.businessType.label")),
          Messages("fh.business_partners.entity_type.individual.label"),
          None,
          GroupRow.Member
        ),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.businessPartners.individual.firstName.label")),
          individual.firstName,
          None,
          GroupRow.Member
        ),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.businessPartners.individual.lastName.label")),
          individual.lastName,
          None,
          GroupRow.Member
        ),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      ),
      if (individual.hasNino) {
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(Messages("fh.businessPartners.individual.nino.label")),
            individual.nino,
            None
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams.ofBoolean(
            Some(Messages("fh.businessPartners.individual.nino.label")),
            individual.hasNino,
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      },
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.businessPartners.individual.address.label")),
          Helpers.formatAddress(individual.address),
          None,
          GroupRow.Member),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      )
    )
}
