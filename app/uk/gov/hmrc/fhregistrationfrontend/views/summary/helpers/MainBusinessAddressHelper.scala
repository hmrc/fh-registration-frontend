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
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.Helpers

object MainBusinessAddressHelper {
  def apply(mainBusinessAddressForm: models.MainBusinessAddress, mode: Mode)(implicit messages: Messages) = {
    val mainBusinessAddress =
      Seq(
        Helpers.createSummaryRow(
          SummaryRowParams.ofString(
            Some(messages("fh.mainBusinessAddress.summary.yearsAtAddress")),
            mainBusinessAddressForm.timeAtCurrentAddress,
            None,
            GroupRow.Single
          ),
          Helpers.createChangeLink(
            Mode isEditable mode,
            "form/mainBusinessAddress/years-at-current-address",
            Text("Change"),
            Some(messages("fh.mainBusinessAddress.summary.yearsAtAddress"))
          )
        )
      )

    val addressLessThanThreeYears = {
      val hasPreviousAddress = mainBusinessAddressForm.hasPreviousAddress
      val optPreviousAddress = mainBusinessAddressForm.previousAddress

      (hasPreviousAddress, optPreviousAddress) match {
        case (Some(false), _) =>
          Seq(
            Helpers.createSummaryRow(
              SummaryRowParams.ofString(
                label = Some(messages("fh.main_business_address.previous.label")),
                value = Some("No"),
                None,
                GroupRow.Single
              ),
              Helpers.createChangeLink(
                Mode isEditable mode,
                "form/mainBusinessAddress/any-previous-business-address",
                Text("Change"),
                Some(messages("fh.mainBusinessAddress.summary.hasPreviousAddress"))
              )
            )
          )
        case (Some(true), Some(previousAddress)) =>
          Seq(
            Helpers.createSummaryRow(
              SummaryRowParams.ofString(
                label = Some(messages("fh.main_business_address.previous.label")),
                value = Some("Yes"),
                None,
                GroupRow.Single
              ),
              Helpers.createChangeLink(
                Mode isEditable mode,
                "form/mainBusinessAddress/any-previous-business-address",
                Text("Change"),
                Some(messages("fh.mainBusinessAddress.summary.hasPreviousAddress"))
              )
            ),
            Helpers.createSummaryRow(
              SummaryRowParams.ofString(
                Some(messages("fh.mainBusinessAddress.summary.previousAddress")),
                Helpers.formatAddress(previousAddress),
                None,
                GroupRow.Single
              ),
              Helpers.createChangeLink(
                Mode isEditable mode,
                "form/mainBusinessAddress/previous-business-address",
                Text("Change"),
                Some(messages("fh.mainBusinessAddress.summary.previousAddress"))
              )
            ),
            Helpers.createSummaryRow(
              SummaryRowParams.ofDate(
                Some(messages("fh.mainBusinessAddress.summary.previousAddressStartdate")),
                mainBusinessAddressForm.previousAddressStartdate,
                None,
                GroupRow.Single
              ),
              Helpers.createChangeLink(
                Mode isEditable mode,
                "form/mainBusinessAddress/previous-business-address",
                Text("Change"),
                Some(messages("fh.mainBusinessAddress.summary.previousAddressStartdate"))
              )
            )
          )
        case (Some(true), _) => Seq.empty
        case _               => Seq.empty
      }
    }
    if (mainBusinessAddressForm.timeAtCurrentAddress == "Less than 3 years") {
      mainBusinessAddress ++ addressLessThanThreeYears
    } else {
      mainBusinessAddress
    }
  }
}
