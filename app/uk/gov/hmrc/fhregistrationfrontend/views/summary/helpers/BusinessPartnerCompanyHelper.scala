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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerCorporateBody
object BusinessPartnerCompanyHelper {

  def apply(company: BusinessPartnerCorporateBody)(implicit messages: Messages): Seq[SummaryListRow] =
    Seq(
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.summary.partnerLegalEntity")),
          Messages("fh.business_partners.entity_type.corporateBody.label"),
          None,
          GroupRow.Member
        ),
        None
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.business_partners.corporate_body.name.label")),
          company.companyName,
          None,
          GroupRow.Member
        ),
        None
      ),
      if (company.hasTradeName) {
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(Messages("fh.tradingName.title")),
            company.tradeName,
            None
          ),
          None
        )
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams.ofBoolean(
            Some(Messages("fh.summary.partnerTradingName")),
            company.hasTradeName,
            None,
            GroupRow.Member
          ),
          None
        )
      },
      if (company.hasVat) {
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(Messages("fh.vatNumber.label")),
            company.vat,
            None
          ),
          None
        )
      } else {
        Helpers.createSummaryRow(
          SummaryRowParams.ofBoolean(
            Some(Messages("fh.summary.partnerVat")),
            company.hasVat,
            None,
            GroupRow.Member
          ),
          None
        )
      },
      Helpers.createSummaryRow(
        SummaryRowParams(
          Some(Messages("fh.ct_utr.label")),
          company.uniqueTaxpayerReference,
          None
        ),
        None
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.company_registration_number.title")),
          company.companyRegistrationNumber,
          None,
          GroupRow.Member
        ),
        None
      ),
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.summary.partnerAddress")),
          Helpers.formatAddress(company.address),
          None,
          GroupRow.Bottom
        ),
        None
      )
    )
}
