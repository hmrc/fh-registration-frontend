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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Journeys
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.views.{ViewHelpers, Views}

@Inject
class SummaryController @Inject()(
  ds: CommonPlayDependencies,
  cc: MessagesControllerComponents,
  actions: Actions,
  journeys: Journeys,
  views: Views
) extends AppController(ds, cc) with SummaryFunctions {

  import actions._
  def summary() = summaryAction { implicit request =>
    val application = request.businessType match {
      case BusinessType.CorporateBody =>
        journeys ltdApplication request
      case BusinessType.SoleTrader =>
        journeys soleTraderApplication request
      case BusinessType.Partnership =>
        journeys partnershipApplication request
    }
    Ok(
      getSummaryHtml(application, request.bpr, request.verifiedEmail, summaryPageParams(request.journeyRequest), views))
  }
}
