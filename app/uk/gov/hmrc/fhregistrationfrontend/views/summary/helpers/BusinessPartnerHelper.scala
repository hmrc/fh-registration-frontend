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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessCustomers => BusinessCustomersModel}
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartner
import uk.gov.hmrc.fhregistrationfrontend.views.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.html.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.html.summary._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessPartnerIndividual => BusinessPartnerIndividualModel}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerCorporateBody
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessPartnerSoleProprietor => BusinessPartnerSoleProprietorModel}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerLimitedLiabilityPartnership
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ListWithTrackedChanges
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessPartnerPartnership => BusinessPartnerPartnershipModel}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessPartnerUnincorporatedBody => BusinessPartnerUnincorporatedBodyModel}
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.{ActionItem, Actions, Text, SummaryListRow}

object BusinessPartnerHelper {

  def apply(businessPartners: ListWithTrackedChanges[BusinessPartner], mode: Mode)(implicit messages: Messages):Seq[SummaryListRow] = {















    Seq(Helpers.createSummaryRow(
      SummaryRowParams(
        Some(Messages("fh.vatNumber.label")),
        Some("value"),
        None,
        GroupRow.Single
      ),
      None
    ))
  }

}
