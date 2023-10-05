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

import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.TradingNameForm.tradingNameForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject

class BusinessPartnersCorporateBodyTradingNameController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._

  val businessType = "corporateBody"
  val companyName = "Shelby Limited"
  val backLink = routes.BusinessPartnersCorporateBodyCompanyNameController.load().url

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      val postAction: Call = routes.BusinessPartnersCorporateBodyTradingNameController.next()
      Ok(
        view
          .business_partners_has_trading_name(tradingNameForm, businessType, companyName, postAction, backLink))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      lazy val postAction: Call = routes.BusinessPartnersCorporateBodyTradingNameController.next()
      tradingNameForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(
              view.business_partners_has_trading_name(formWithErrors, businessType, companyName, postAction, backLink))
          },
          tradingName => {
            Redirect(routes.BusinessPartnersCorporateBodyCompanyRegistrationNumberController.load())
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

}
