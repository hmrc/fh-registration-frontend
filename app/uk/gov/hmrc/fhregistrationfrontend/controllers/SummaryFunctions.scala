/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.mvc.{AnyContent, Request}
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.actions.{JourneyRequest, SummaryRequest}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{JourneyType, Journeys}
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.FhddsStatus
import uk.gov.hmrc.fhregistrationfrontend.views.{Mode, Views}
import uk.gov.hmrc.fhregistrationfrontend.views.html.summary.SummaryPrintable
import uk.gov.hmrc.fhregistrationfrontend.views.html.{ltd_summary, partnership_summary, sole_proprietor_summary}
import uk.gov.hmrc.fhregistrationfrontend.views.summary.SummaryPageParams

trait SummaryFunctions {
  this: AppController ⇒

  protected def getSummaryHtml(
    application: BusinessEntityApplication,
    bpr: BusinessRegistrationDetails,
    verifiedEmail: String,
    params: SummaryPageParams,
    views: Views)(implicit request: Request[AnyContent]): Html =
    application match {
      case a: LimitedCompanyApplication ⇒
        views.ltd_summary(a, bpr, verifiedEmail, None, params)
      case a: SoleProprietorApplication ⇒
        views.sole_proprietor_summary(a, bpr, verifiedEmail, None, params)
      case a: PartnershipApplication ⇒
        views.partnership_summary(a, bpr, verifiedEmail, None, params)
    }

  protected def getSummaryPrintable(journeys: Journeys)(implicit request: SummaryRequest[AnyContent]) = {
    val application = request.businessType match {
      case BusinessType.CorporateBody ⇒ journeys ltdApplication request
      case BusinessType.SoleTrader ⇒ journeys soleTraderApplication request
      case BusinessType.Partnership ⇒ journeys partnershipApplication request
    }

    SummaryPrintable(application, request.bpr, request.verifiedEmail)
  }

  protected def readOnlySummaryPageParams(status: FhddsStatus) = {
    val mode = status match {
      case FhddsStatus.Deregistered | FhddsStatus.Revoked ⇒ Mode.ReadOnlyRegister
      case _ ⇒ Mode.ReadOnlyApplication

    }
    SummaryPageParams(mode, None)
  }

  protected def summaryPageParams(journeyRequest: JourneyRequest[_]) =
    SummaryPageParams(modeForJourneyType(journeyRequest.journeyType), journeyRequest.hasUpdates)

  protected def summaryPageParams(journeyType: JourneyType, hasUpdates: Option[Boolean] = None) =
    SummaryPageParams(modeForJourneyType(journeyType), hasUpdates)

  protected def modeForJourneyType(journeyType: JourneyType) = journeyType match {
    case JourneyType.New ⇒ Mode.New
    case JourneyType.Variation ⇒ Mode.Variation
    case JourneyType.Amendment ⇒ Mode.Amendment
  }

}
