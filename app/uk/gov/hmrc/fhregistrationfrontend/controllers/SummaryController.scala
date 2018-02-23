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

import uk.gov.hmrc.fhregistrationfrontend.actions.SummaryAction
import uk.gov.hmrc.fhregistrationfrontend.forms.models.LimitedCompanyApplication
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.html.ltd_summary

@Inject
class SummaryController @Inject()(
  ds               : CommonPlayDependencies,
  messagesApi      : play.api.i18n.MessagesApi,
  links            : ExternalUrls
)(implicit save4LaterService: Save4LaterService) extends AppController(ds, messagesApi) {

  def summary() = SummaryAction(save4LaterService) { implicit request ⇒
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
