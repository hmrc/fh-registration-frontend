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

import uk.gov.hmrc.fhregistrationfrontend.forms.models.{ImportingActivities => ImportingActivitiesModel}
import uk.gov.hmrc.fhregistrationfrontend.views.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.html.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.{ActionItem, Actions, Text}
import play.api.i18n.Messages

object ImportingActivitiesHelper {

  def apply(importingActivitiesForm: ImportingActivitiesModel, mode: Mode)(implicit messages: Messages) = {

    val changeLink = {
      if (Mode isEditable mode) {
        Some("form/importingActivities")
      } else {
        None
      }
    }

    Seq(if (importingActivitiesForm.hasEori) {

      Helpers.createSummaryRow(
        SummaryRowParams(
          Some(Messages("fh.importing_activities.eori.label")),
          importingActivitiesForm.eoriNumber.map(_.eoriNumber),
          changeLink,
          GroupRow.Top
        ),
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href = changeLink.get,
                content = Text("Change"),
                visuallyHiddenText = Some(Messages("fh.importing_activities.eori.label"))
              )
            )))
      )

      Helpers.createSummaryRow(
        SummaryRowParams.ofBoolean(
          Some(Messages("fh.summary.usesEORI")),
          importingActivitiesForm.eoriNumber.map(_.goodsImportedOutsideEori),
          None,
          GroupRow.Bottom
        ),
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href = changeLink.get,
                content = Text("Change"),
                visuallyHiddenText = Some(Messages("fh.summary.usesEORI"))
              )
            )))
      )

    } else {
      Helpers.createSummaryRow(
        SummaryRowParams.ofBoolean(
          Some(Messages("fh.summary.hasEORI")),
          importingActivitiesForm.hasEori,
          changeLink,
          GroupRow.Single
        ),
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href = changeLink.get,
                content = Text("Change"),
                visuallyHiddenText = Some(Messages("fh.summary.hasEORI"))
              )
            )))
      )
    })
  }

}
