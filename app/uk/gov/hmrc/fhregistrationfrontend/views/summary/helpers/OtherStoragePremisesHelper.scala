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

import uk.gov.hmrc.fhregistrationfrontend.forms.models.{OtherStoragePremises => StoragePremisesModel}
import uk.gov.hmrc.fhregistrationfrontend.views.helpers._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.StoragePremise
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.{ActionItem, Actions, Text}
import play.api.i18n.Messages

object OtherStoragePremisesHelper {

  def apply(data: StoragePremisesModel, mode: Mode)(implicit messages: Messages) = {

    val storageAddress = {
      data.value.values.zipWithIndex.flatMap {
        case (storagePremise: StoragePremise, index) =>
          val address = Helpers.formatAddress(storagePremise.address)
          Seq(
            Helpers.createSummaryRow(
              SummaryRowParams.ofBoolean(
                Some(Messages("fh.summary.thirdPartyPremises")),
                storagePremise.isThirdParty,
                None,
                GroupRow.Bottom
              ),
              Helpers.createChangeLink(
                Mode isEditable mode,
                s"form/otherStoragePremises/${index + 1}",
                Text("Change"),
                Some(Messages("fh.other_storage_premises.each.title", {
                  index + 1
                })))
            ),
            Helpers.createSummaryRow(
              SummaryRowParams(Some(Messages("fh.other_storage_premises.each.title", {
                index + 1
              })), Some(address), None, GroupRow.Single),
              Helpers.createChangeLink(
                Mode isEditable mode,
                s"form/otherStoragePremises/${index + 1}",
                Text("Change"),
                Some(Messages("fh.other_storage_premises.each.title", {
                  index + 1
                })))
            )
          )
      }.toSeq
    }

    val storagePremise = Seq(
      Helpers.createSummaryRow(
        SummaryRowParams.ofBoolean(
          Some(Messages("fh.summary.usesStoragePremises")),
          data.hasValue,
          None,
          GroupRow.Single
        ),
        Helpers.createChangeLink(
          Mode isEditable mode,
          s"form/otherStoragePremises",
          Text("Change"),
          Some(Messages("fh.other_storage_premises.title"))
        )
      )
    )

    if (data.hasValue) {
      storagePremise ++ storageAddress
    } else {
      storagePremise
    }
  }
}
