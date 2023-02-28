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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.NationalInsuranceNumber
import uk.gov.hmrc.fhregistrationfrontend.views.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.Text

object NationalInsuranceHelper {

  def apply(nationalInsuranceForm: NationalInsuranceNumber, mode: Mode)(implicit messages: Messages) = {

    val changeLink =
      if (Mode isEditable mode) {
        Some("nationalInsuranceNumber")
      } else {
        None
      }

    Seq(if (nationalInsuranceForm.hasValue) {
      Helpers.createSummaryRow(
        SummaryRowParams(
          Some(Messages("fh.national_insurance_number.label")),
          nationalInsuranceForm.value,
          changeLink
        ),
        Helpers.createChangeLink(
          Mode isEditable mode,
          "form/nationalInsuranceNumber",
          Text("Change"),
          Some(Messages("fh.company_registration_number.title"))
        )
      )
    } else {
      Helpers.createSummaryRow(
        SummaryRowParams.ofBoolean(
          Some(Messages("fh.summary.HasNino")),
          nationalInsuranceForm.hasValue,
          changeLink,
          GroupRow.Single
        ),
        Helpers.createChangeLink(
          Mode isEditable mode,
          "form/nationalInsuranceNumber",
          Text("Change"),
          Some(Messages("fh.company_registration_number.title"))
        )
      )
    })
  }
}
