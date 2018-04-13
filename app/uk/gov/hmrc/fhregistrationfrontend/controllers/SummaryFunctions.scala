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

import play.api.mvc.AnyContent
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.actions.SummaryRequest
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Journeys
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.views.html.summary.SummaryPrintable
import uk.gov.hmrc.fhregistrationfrontend.views.html.{ltd_summary, partnership_summary, sole_proprietor_summary}


trait SummaryFunctions {
  this: AppController ⇒

  protected def getSummaryHtml(request: SummaryRequest[AnyContent], hasAmendments: Option[Boolean] = None): Html = {

    val url = None

    request.businessType match {
      case BusinessType.CorporateBody ⇒ ltdSummary(url, hasAmendments)(request)
      case BusinessType.SoleTrader ⇒ soleTrader(url, hasAmendments)(request)
      case BusinessType.Partnership ⇒ partnership(url, hasAmendments)(request)
    }
  }

  protected def partnership(baseUrl: Option[String], hasAmendments: Option[Boolean] = None)(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys partnershipApplication request
    partnership_summary(application, request.bpr, baseUrl, hasAmendments)
  }


  protected def soleTrader(baseUrl: Option[String], hasAmendments: Option[Boolean] = None)(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys soleTraderApplication request
    sole_proprietor_summary(application, request.bpr, baseUrl, hasAmendments)
  }

  protected def ltdSummary(baseUrl: Option[String], hasAmendments: Option[Boolean] = None)(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys ltdApplication request

    ltd_summary(application, request.bpr, baseUrl, hasAmendments)
  }

  protected def getSummaryData()(implicit request: SummaryRequest[AnyContent]) = {
    val application = request.businessType match {
      case BusinessType.CorporateBody ⇒ Journeys ltdApplication request
      case BusinessType.SoleTrader ⇒ Journeys soleTraderApplication request
      case BusinessType.Partnership ⇒ Journeys partnershipApplication request
    }

    SummaryPrintable(application, request.bpr)
  }

}
