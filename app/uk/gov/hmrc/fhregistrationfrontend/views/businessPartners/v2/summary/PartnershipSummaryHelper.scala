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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerPartnership
import uk.gov.hmrc.fhregistrationfrontend.views.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.{SummaryListRow, Text}

object PartnershipSummaryHelper {
  def apply(partnership: BusinessPartnerPartnership)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.businessPartners.summary.businessType.label")),
          messages("fh.business_partners.entity_type.partnership.label"),
          None,
          GroupRow.Member
        ),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.businessPartners.summary.partnershipName")),
          partnership.partnershipName,
          None,
          GroupRow.Member
        ),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      ),
      if (partnership.hasTradeName) {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.tradingName.label")),
            partnership.tradeName,
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
      if (partnership.hasVat) {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.businessPartners.summary.vatRegistrationNumber")),
            partnership.vat,
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.businessPartners.summary.vatRegistrationNumber")),
            messages("fh.businessPartners.summary.none"),
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      },
      if (partnership.hasUniqueTaxpayerReference) {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.businessPartners.summary.utr")),
            partnership.uniqueTaxpayerReference,
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.businessPartners.summary.utr")),
            messages("fh.businessPartners.summary.none"),
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      },
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.businessPartners.summary.address")),
          Helpers.formatAddress(partnership.address),
          None,
          GroupRow.Member),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      )
    )
}
