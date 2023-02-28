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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessPartnerSoleProprietor => BusinessPartnerSoleProprietorModel}

object BusinessPartnerSoleProprietorHelper {
  def apply(partner: BusinessPartnerSoleProprietorModel)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.summary.partnerLegalEntity")),
          Messages("fh.business_partners.entity_type.sole_proprietor.label"),
          None,
          GroupRow.Member
        ),
        None
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.generic.name")),
          partner.firstName + " " + partner.lastName,
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
      if (partner.hasNino) {
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(Messages("fh.business_partners.individual.nino.label")),
            partner.nino,
            None
          ),
          None)
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams.ofBoolean(
            Some(Messages("fh.summary.partnerHasNino")),
            partner.hasNino,
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
          SummaryRowParams.ofBoolean(
            Some(Messages("fh.summary.partnerVat")),
            partner.hasVat,
            None,
            GroupRow.Member
          ),
          None)
      },
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
