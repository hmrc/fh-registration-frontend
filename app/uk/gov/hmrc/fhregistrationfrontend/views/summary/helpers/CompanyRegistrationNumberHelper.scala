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
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.{Helpers, SummaryRowParams}
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.{ActionItem, Actions, Text}
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.CompanyRegistrationNumber

object CompanyRegistrationNumberHelper {

  def apply(companyRegistrationNumberForm: CompanyRegistrationNumber, mode: Mode)(
    implicit messages: Messages): Seq[SummaryListRow] = {

    val CompanyRegistrationNumber = {
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.company_registration_number.title")),
          companyRegistrationNumberForm.crn,
          None,
          GroupRow.Single
        ),
        Helpers.createChangeLink(
          Mode isEditable mode,
          "form/companyRegistrationNumber",
          Text("Change"),
          Some(Messages("fh.company_registration_number.title"))
        )
      )
    }
    Seq(CompanyRegistrationNumber)
  }

}
