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
import uk.gov.hmrc.govukfrontend.views.html.components.Text
import uk.gov.hmrc.fhregistrationfrontend.forms.models.DateOfIncorporation

object DateOfIncorporationHelper {

  def apply(dateOfIncorporationForm: DateOfIncorporation, mode: Mode)(implicit messages: Messages) = {

    val DateOfIncorporation =
      Helpers.createSummaryRow(
        SummaryRowParams.ofDate(
          Some(Messages("fh.date_of_incorporation.title")),
          dateOfIncorporationForm.dateOfIncorporation,
          None,
          GroupRow.Single
        ),
        Helpers.createChangeLink(
          Mode isEditable mode,
          "form/dateOfIncorporation",
          Text("Change"),
          Some(Messages("fh.date_of_incorporation.title"))
        )
      )
    Seq(DateOfIncorporation)
  }
}
