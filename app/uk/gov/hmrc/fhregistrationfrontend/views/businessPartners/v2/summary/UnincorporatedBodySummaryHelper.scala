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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessPartnerLimitedLiabilityPartnership, BusinessPartnerUnincorporatedBody}
import uk.gov.hmrc.fhregistrationfrontend.views.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.{SummaryListRow, Text}

object UnincorporatedBodySummaryHelper {
  def apply(llp: BusinessPartnerUnincorporatedBody)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.businessPartners.summary.businessType.label")),
          messages("fh.businessPartners.unincorporatedBody.summary.label"),
          None,
          GroupRow.Member
        ),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.businessPartners.unincorporatedBody.summary.unincorporatedBodyName.label")),
          llp.unincorporatedBodyName,
          None,
          GroupRow.Member
        ),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      ),
      if (llp.hasTradeName) {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.tradingName.label")),
            llp.tradeName,
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.tradingName.label")),
            messages("fh.businessPartners.summary.none"),
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      },
      if (llp.hasVat) {
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(messages("fh.businessPartners.vatNumber.label")),
            llp.vat,
            None
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.businessPartners.vatNumber.label")),
            messages("fh.businessPartners.summary.none"),
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      },
      if (llp.hasUniqueTaxpayerReference) {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.businessPartners.unincorporatedBody.summary.saUtr")),
            llp.uniqueTaxpayerReference,
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.businessPartners.unincorporatedBody.summary.saUtr")),
            llp.uniqueTaxpayerReference,
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      },
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.businessPartners.llp.summary.address")),
          Helpers.formatAddress(llp.address),
          None,
          GroupRow.Member),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      )
    )
}
