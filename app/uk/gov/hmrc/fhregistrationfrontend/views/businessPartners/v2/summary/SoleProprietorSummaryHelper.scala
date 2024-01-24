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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerSoleProprietor
import uk.gov.hmrc.fhregistrationfrontend.views.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.{SummaryListRow, Text}

object SoleProprietorSummaryHelper {
  def apply(soleProprietor: BusinessPartnerSoleProprietor)(implicit messages: Messages): Seq[SummaryListRow] = {
    val base = Seq(
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.businessPartners.summary.businessType.label")),
          messages("fh.business_partners.entity_type.soleProprietor.label"),
          None,
          GroupRow.Member
        ),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.businessPartners.soleProprietor.firstName.label")),
          soleProprietor.firstName,
          None,
          GroupRow.Member
        ),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(messages("fh.businessPartners.soleProprietor.lastName.label")),
          soleProprietor.lastName,
          None,
          GroupRow.Member
        ),
        Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
      ),
      if (soleProprietor.hasTradeName) {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.tradingName.label")),
            soleProprietor.tradeName,
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
      if (soleProprietor.hasNino) {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.national_insurance_number.label")),
            soleProprietor.nino,
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.national_insurance_number.label")),
            messages("fh.businessPartners.summary.none"),
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      }
    )

    val extraFields = if (soleProprietor.hasVat) {
      Seq(
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(messages("fh.vatNumber.label")),
            soleProprietor.vat,
            None
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        ))
    } else
      Seq(
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.businessPartners.vatNumber.label")),
            messages("fh.businessPartners.summary.none"),
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        ),
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.businessPartners.soleProprietor.utr.label")),
            soleProprietor.uniqueTaxpayerReference,
            None,
            GroupRow.Member
          ),
          Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
        )
      )

    val addressRow = Helpers.createSummaryRow(
      SummaryRowParams.ofString(
        Some(messages("fh.businessPartners.soleProprietor.address.label")),
        Helpers.formatAddress(soleProprietor.address),
        None,
        GroupRow.Member),
      Helpers.createChangeLink(isEditable = true, "#", Text("Change"), Some(messages("hidden text")))
    )

    base ++ extraFields :+ addressRow
  }
}
