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
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.DesToForm
import uk.gov.hmrc.fhregistrationfrontend.views.{ViewHelpers, Views}

import scala.concurrent.ExecutionContext

class ReadOnlySummaryController @Inject() (
  ds: CommonPlayDependencies,
  desToForm: DesToForm,
  fhddsConnector: FhddsConnector,
  cc: MessagesControllerComponents,
  actions: Actions,
  views: Views
)(implicit ec: ExecutionContext)
    extends AppController(ds, cc) with SummaryFunctions {

  def view() = actions.enrolledUserAction.async { implicit request =>
    for {
      status         <- fhddsConnector getStatus request.registrationNumber
      displayWrapper <- fhddsConnector getSubmission request.registrationNumber
      display = displayWrapper.subScriptionDisplay
    } yield {
      val application = desToForm loadApplicationFromDes display
      val bpr = desToForm.businessRegistrationDetails(display)
      val contactEmail = desToForm.contactEmail(display)

      Ok(getSummaryHtml(application, bpr, contactEmail.get, readOnlySummaryPageParams(status), views))
    }
  }
}
