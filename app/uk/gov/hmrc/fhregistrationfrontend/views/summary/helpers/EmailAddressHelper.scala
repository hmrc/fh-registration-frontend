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

import uk.gov.hmrc.fhregistrationfrontend.views.html.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.fhregistrationfrontend.views.ViewHelpers
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.Helpers
import uk.gov.hmrc.fhregistrationfrontend.views.summary._
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.SummaryRowParams
import play.api.i18n.Messages

object EmailAddressHelper {
  def apply(verifiedEmail: String, mode: Mode)(implicit messages: Messages) = {
    val changeLink = Helpers.createChangeLink(
      Mode isEditable mode,
      "email-edit",
      Text("Change"),
      Some(Messages("fh.summary.email.label"))
    )

    Seq(
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(Some(Messages("fh.summary.email.label")), verifiedEmail, None, GroupRow.Single),
        changeLink
      )
    )
  }
}
