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
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.govukfrontend.views.html.components.Text
import play.api.i18n.Messages

object ImportingActivitiesHelper {

  def apply(importingActivitiesForm: ImportingActivitiesModel, mode: Mode)(implicit messages: Messages) = {
    if (importingActivitiesForm.hasEori) {
      Seq(
        Helpers.createSummaryRow(
          SummaryRowParams.ofBoolean(
            Some(Messages("fh.summary.hasEORI")),
            importingActivitiesForm.hasEori,
            None,
            GroupRow.Single
          ),
          Helpers.createChangeLink(
            Mode isEditable mode,
            "form/importingActivities",
            Text("Change"),
            Some(Messages("fh.summary.hasEORI"))
          )
        ),
        Helpers.createSummaryRow(
          SummaryRowParams(
            Some(Messages("fh.importing_activities.eori.label")),
            importingActivitiesForm.eoriNumber.map(_.eoriNumber),
            None,
            GroupRow.Top
          ),
          Helpers.createChangeLink(
            Mode isEditable mode,
            "form/importingActivities/eoriNumber",
            Text("Change"),
            Some(Messages("fh.importing_activities.eori.label")))
        ),
        Helpers.createSummaryRow(
          SummaryRowParams.ofBoolean(
            Some(Messages("fh.summary.usesEORI")),
            importingActivitiesForm.eoriNumber.map(_.goodsImportedOutsideEori),
            None,
            GroupRow.Bottom
          ),
          Helpers.createChangeLink(
            Mode isEditable mode,
            "form/importingActivities/goods",
            Text("Change"),
            Some(Messages("fh.summary.usesEORI"))
          )
        )
      )
    } else {
      Seq(
        Helpers.createSummaryRow(
          SummaryRowParams.ofBoolean(
            Some(Messages("fh.summary.hasEORI")),
            importingActivitiesForm.hasEori,
            None,
            GroupRow.Single
          ),
          Helpers.createChangeLink(
            Mode isEditable mode,
            "form/importingActivities",
            Text("Change"),
            Some(Messages("fh.summary.hasEORI"))
          )
        )
      )
    }
  }

}
