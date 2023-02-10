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
import uk.gov.hmrc.fhregistrationfrontend.forms.models
import uk.gov.hmrc.fhregistrationfrontend.views.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.html.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.Helpers

object MainBusinessAddressHelper {
  def apply(mainBusinessAddressForm: models.MainBusinessAddress, mode: Mode)(implicit messages: Messages) = {
    val isGroupHead =
      if (mainBusinessAddressForm.timeAtCurrentAddress == "Less than 3 years") GroupRow.Top else GroupRow.Single

    val mainBusinessAddress =
      Seq(
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(Messages("fh.mainBusinessAddress.summary.yearsAtAddress")),
            mainBusinessAddressForm.timeAtCurrentAddress,
            None,
            isGroupHead
          ),
          Helpers.createChangeLink(
            Mode isEditable mode,
            "mainBusinessAddress",
            Text("Change"),
            Some(Messages("fh.mainBusinessAddress.summary.yearsAtAddress"))
          )
        ))

    val addressLessThanThreeYears = {
      val hasPreviousAddress = mainBusinessAddressForm.hasPreviousAddress
      val optPreviousAddress = mainBusinessAddressForm.previousAddress

      (hasPreviousAddress, optPreviousAddress) match {
        case (Some(false), _) =>
          Seq(
            Helpers.createSummaryRow(
              SummaryRowParams.ofString(
                Some(Messages("fh.mainBusinessAddress.summary.yearsAtAddress")),
                mainBusinessAddressForm.timeAtCurrentAddress,
                None,
                isGroupHead
              ),
              None
            )
          )
        case (Some(true), Some(previousAddress)) =>
          Seq(
            Helpers.createSummaryRow(
              SummaryRowParams.ofString(
                Some(Messages("fh.mainBusinessAddress.summary.previousAddress")),
                Helpers.formatAddress(previousAddress),
                None,
                GroupRow.Member
              ),
              None
            ),
            Helpers.createSummaryRow(
              SummaryRowParams.ofDate(
                Some(Messages("fh.mainBusinessAddress.summary.previousAddressStartdate")),
                mainBusinessAddressForm.previousAddressStartdate,
                None,
                GroupRow.Bottom
              ),
              None
            )
          )
        case (Some(true), _) =>
          Seq(
            Helpers.createSummaryRow(
              SummaryRowParams.ofDate(
                Some(Messages("fh.mainBusinessAddress.summary.previousAddressStartdate")),
                mainBusinessAddressForm.previousAddressStartdate,
                None,
                GroupRow.Bottom
              ),
              None
            )
          )
        case _ => Seq.empty
      }
    }
    if (mainBusinessAddressForm.timeAtCurrentAddress == "Less than 3 years") {
      mainBusinessAddress
    } else {
      mainBusinessAddress ++ addressLessThanThreeYears
    }
  }
}
