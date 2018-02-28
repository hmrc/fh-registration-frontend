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

import play.api.mvc.AnyContent
import uk.gov.hmrc.fhregistrationfrontend.actions.{SummaryAction, SummaryRequest}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Journeys
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessType, LimitedCompanyApplication, PartnershipApplication, SoleProprietorApplication}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.html.ltd_summary
import uk.gov.hmrc.fhregistrationfrontend.views.html.sole_proprietor_summary
import uk.gov.hmrc.fhregistrationfrontend.views.html.partnership_summary

@Inject
class SummaryController @Inject()(
  ds               : CommonPlayDependencies,
  messagesApi      : play.api.i18n.MessagesApi,
  links            : ExternalUrls
)(implicit save4LaterService: Save4LaterService) extends AppController(ds, messagesApi) {



  def summary() = SummaryAction(save4LaterService) { implicit request ⇒
    request.businessType match {
      case BusinessType.CorporateBody ⇒ ltdSummary(request)
      case BusinessType.SoleTrader ⇒ soleTrader(request)
      case BusinessType.Partnership ⇒ partnership(request)
    }
  }


  def partnership(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys partnershipApplication request
    Ok(partnership_summary(application, request.bpr))
  }


  def soleTrader(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys soleTraderApplication request

    Ok(sole_proprietor_summary(application, request.bpr))
  }

  private def ltdSummary(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys ltdApplication request
    Ok(ltd_summary(application, request.bpr))
  }
}
