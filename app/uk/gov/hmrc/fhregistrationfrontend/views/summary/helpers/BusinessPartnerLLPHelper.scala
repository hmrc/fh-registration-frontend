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
import uk.gov.hmrc.fhregistrationfrontend.views.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.SummaryListRow
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerLimitedLiabilityPartnership

object BusinessPartnerLLPHelper {
  def apply(partner: BusinessPartnerLimitedLiabilityPartnership)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.summary.partnerLegalEntity")),
          Messages("fh.business_partners.entity_type.limited_liability_partnership.label"),
          None,
          GroupRow.Member
        ),
        None
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.business_partners.liability_partnership.name.label")),
          partner.limitedLiabilityPartnershipName,
          None,
          GroupRow.Member
        ),
        None
      ),
      if (partner.hasTradeName) {
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(Messages("fh.tradingName.title")),
            partner.tradeName,
            None
          ),
          None)
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams.ofBoolean(
            Some(Messages("fh.summary.partnerTradingName")),
            partner.hasTradeName,
            None,
            GroupRow.Member
          ),
          None)
      },
      if (partner.hasVat) {
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(Messages("fh.vatNumber.label")),
            partner.vat,
            None
          ),
          None)
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(Messages("fh.ct_utr.label")),
            partner.uniqueTaxpayerReference,
            None
          ),
          None)
      },
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.company_registration_number.title")),
          partner.companyRegistrationNumber,
          None,
          GroupRow.Member
        ),
        None),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.summary.partnerAddress")),
          Helpers.formatAddress(partner.address),
          None,
          GroupRow.Bottom),
        None
      )
    )
}
