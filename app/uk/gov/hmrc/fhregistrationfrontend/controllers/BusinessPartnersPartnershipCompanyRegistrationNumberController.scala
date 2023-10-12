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

import com.google.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.CompanyRegistrationNumberForm._
import uk.gov.hmrc.fhregistrationfrontend.views.Views

@Singleton
class BusinessPartnersPartnershipCompanyRegistrationNumberController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {
  import actions._

  val businessType: String = "limited-liability-partnership"
  val companyName: String = "Test Partnership"
  val backLink: String = routes.BusinessPartnerPartnershipTradingNameController.load().url
  val postAction: Call = routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.next()

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      //ToDo read this data from the cache after being stored before the redirect
      Ok(
        view.business_partners_company_reg_number(
          companyRegistrationNumberForm,
          companyName,
          businessType,
          postAction,
          backLink))
        .withCookies(Cookie("businessType", businessType))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      //ToDo read this data from the cache after being stored before the redirect
      companyRegistrationNumberForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(
              view
                .business_partners_company_reg_number(formWithErrors, companyName, businessType, postAction, backLink))
          },
          regNumber => {
            Redirect(routes.BusinessPartnersPartnershipVatNumberController.load())
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
}
