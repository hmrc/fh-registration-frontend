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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessPartnerPartnership => BusinessPartnerPartnershipModel}

object BusinessPartnerPartnershipHelper {

  def apply(partner: BusinessPartnerPartnershipModel)(implicit messages: Messages): Seq[SummaryListRow] = {

    val partnerHasVat = {
      if (partner.hasVat) {
        Seq(
          Helpers.createSummaryRow(
            SummaryRowParams(
              Some(Messages("fh.vatNumber.label")),
              partner.vat,
              None
            ),
            None)
        )
      } else {
        Seq.empty
      }
    }

    val hasUniqueTaxpayerReference = {
      if (partner.hasUniqueTaxpayerReference) {
        Seq(
          Helpers.createSummaryRow(
            SummaryRowParams(
              Some(Messages("fh.ct_utr.label")),
              partner.uniqueTaxpayerReference,
              None
            ),
            None)
        )
      } else {
        Seq.empty
      }
    }

    val businessPartnerPartnership = Seq(
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.summary.partnerLegalEntity")),
          Messages("fh.business_partners.entity_type.partnership.label"),
          None,
          GroupRow.Member
        ),
        None
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.business_partners.partnership.name.label")),
          partner.partnershipName,
          None,
          GroupRow.Member
        ),
        None),
      if (partner.hasTradeName) {
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(Messages("fh.tradingName.title")),
            partner.tradeName,
            None,
            GroupRow.Member
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
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.summary.partnerAddress")),
          Helpers.formatAddress(partner.address),
          None,
          GroupRow.Member),
        None)
    )

    (partner.hasVat, partner.hasUniqueTaxpayerReference) match {
      case (true, true) =>
        businessPartnerPartnership.dropRight(1) ++ partnerHasVat ++ hasUniqueTaxpayerReference ++ Seq(
          businessPartnerPartnership.last)

      case (true, false) =>
        businessPartnerPartnership.dropRight(1) ++ partnerHasVat ++ Seq(businessPartnerPartnership.last)

      case (false, true) =>
        businessPartnerPartnership.dropRight(1) ++ hasUniqueTaxpayerReference ++ Seq(businessPartnerPartnership.last)

      case (false, false) => businessPartnerPartnership
    }
  }
}
