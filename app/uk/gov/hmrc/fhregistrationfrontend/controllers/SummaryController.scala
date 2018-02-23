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
    import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page._
    val application = PartnershipApplication(
      request pageData mainBusinessAddressPage,
      request pageData contactPersonPage,
      request pageData tradingNamePage,
      request pageData vatNumberPage,
      request pageData businessPartnersPage,
      request pageData businessStatusPage,
      request pageData importingActivitiesPage,
      request pageData businessCustomersPage,
      request pageData otherStoragePremisesPage
    )

    Ok(partnership_summary(application, request.bpr))
  }


  def soleTrader(implicit request: SummaryRequest[AnyContent]) = {
    import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page._
    val application = SoleProprietorApplication(
      request pageData mainBusinessAddressPage,
      request pageData contactPersonPage,
      request pageData nationalInsuranceNumberPage,
      request pageData tradingNamePage,
      request pageData vatNumberPage,
      request pageData businessStatusPage,
      request pageData importingActivitiesPage,
      request pageData businessCustomersPage,
      request pageData otherStoragePremisesPage
    )

    Ok(sole_proprietor_summary(application, request.bpr))
  }

  private def ltdSummary(implicit request: SummaryRequest[AnyContent]) = {
    import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page._
    val application = LimitedCompanyApplication(
      request pageData mainBusinessAddressPage,
      request pageData contactPersonPage,
      request pageData companyRegistrationNumberPage,
      request pageData dateOfIncorporationPage,
      request pageData tradingNamePage,
      request pageData vatNumberPage,
      request pageData companyOfficersPage,
      request pageData businessStatusPage,
      request pageData importingActivitiesPage,
      request pageData businessCustomersPage,
      request pageData otherStoragePremisesPage
    )

    Ok(ltd_summary(application, request.bpr))
  }
}
