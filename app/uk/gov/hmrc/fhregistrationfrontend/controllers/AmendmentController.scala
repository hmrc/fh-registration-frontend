/*
 * Copyright 2018 HM Revenue & Customs
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

import uk.gov.hmrc.fhregistrationfrontend.actions.EnrolledUserAction
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{JourneyPages, Journeys}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.des.SubscriptionDisplay
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.DesToForm
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

@Inject
class AmendmentController @Inject()(
  ds               : CommonPlayDependencies,
  links            : ExternalUrls,
  desToForm        : DesToForm,
  fhddsConnector   : FhddsConnector
)(implicit save4LaterService: Save4LaterService) extends AppController(ds) {

  def startAmendment() = EnrolledUserAction().async { implicit request ⇒

    fhddsConnector.getSubmission(request.registrationNumber) flatMap { displayWrapper ⇒
      val display = displayWrapper.subScriptionDisplay
      val userId = request.userId
      val entityType = desToForm entityType display
      val journeyPages = loadJourneyPagesFromDes(display)
      val bpr = desToForm.businessRegistrationDetails(display)

      for {
        _ ← save4LaterService.saveBusinessRegistrationDetails(userId, bpr)
        _ ← save4LaterService.saveBusinessType(userId, entityType)
        _ ← savePageData(userId, journeyPages)
      } yield
        Redirect(routes.SummaryController.summary())
    }
  }

  private def savePageData(userId: String, pages: JourneyPages)(implicit hc: HeaderCarrier) = {
    val ignored: Any = 1
    pages.pages.foldLeft(Future successful ignored) {
      case (acc, page) ⇒  acc flatMap { _ ⇒ save4LaterService.saveData4Later(userId, page.id, page.data.get)(hc, page.format)}
    }
  }

  private def loadJourneyPagesFromDes(display: SubscriptionDisplay) = {
    desToForm.entityType(display) match {
      case BusinessType.CorporateBody ⇒
        val application = desToForm.limitedCompanyApplication(display)
        Journeys unapplyLimitedCompanyApplication application
      case BusinessType.SoleTrader ⇒
        val application = desToForm.soleProprietorApplication(display)
        Journeys unapplySoleTraderApplication application
      case BusinessType.Partnership ⇒
        val application = desToForm partnershipApplication display
        Journeys unapplyPartnershipApplication application
    }
  }

}
