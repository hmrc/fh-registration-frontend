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

import java.util.Date

import play.api.mvc.AnyContent
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.actions.SummaryRequest
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Journeys
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.views.html.{ltd_summary, partnership_summary, sole_proprietor_summary}

trait SummaryFunctions {
  this: AppController ⇒

  protected def getSummaryHtml(request: SummaryRequest[AnyContent], forPrint: Boolean = false, timeStamp: Option[Date] = None, hasAmendments: Option[Boolean] = None): Html = {

    val url = None

    request.businessType match {
      case BusinessType.CorporateBody ⇒ ltdSummary(forPrint, url, timeStamp, hasAmendments)(request)
      case BusinessType.SoleTrader ⇒ soleTrader(forPrint, url, timeStamp, hasAmendments)(request)
      case BusinessType.Partnership ⇒ partnership(forPrint, url, timeStamp, hasAmendments)(request)
    }
  }

  protected def partnership(forPrint: Boolean = false, baseUrl: Option[String], timeStamp: Option[Date] = None, hasAmendments: Option[Boolean] = None)(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys partnershipApplication request
    partnership_summary(application, request.bpr, baseUrl, forPrint, timeStamp, hasAmendments)
  }


  protected def soleTrader(forPrint: Boolean = false, baseUrl: Option[String], timeStamp: Option[Date] = None, hasAmendments: Option[Boolean] = None)(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys soleTraderApplication request
    sole_proprietor_summary(application, request.bpr, baseUrl, forPrint, timeStamp, hasAmendments)
  }

  protected def ltdSummary(forPrint: Boolean = false, baseUrl: Option[String], timeStamp: Option[Date] = None, hasAmendments: Option[Boolean] = None)(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys ltdApplication request

    ltd_summary(application, request.bpr, baseUrl, forPrint, timeStamp, hasAmendments)
  }

}
