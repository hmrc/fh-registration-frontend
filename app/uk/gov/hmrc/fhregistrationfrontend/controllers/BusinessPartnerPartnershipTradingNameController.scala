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

class BusinessPartnerPartnershipTradingNameController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._

  val backUrl: String = {
    if (getBusinessType == "partnership")
      routes.BusinessPartnersPartnershipNameController.load().url
    else if (getBusinessType == "limited-liability-partnership")
      routes.BusinessPartnersLtdLiabilityPartnershipController.load().url
    else "#"
  }

  private def getBusinessType: String = config.getRandomBusinessType

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(view.business_partner_partnership_trading_name(tradingNameForm, "Test User", backAction = backUrl))
        .withCookies(Cookie("businessType", getBusinessType))
        .bakeCookies() // TODO [DLS-7603] - temp save4later solution
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      tradingNameForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(view.business_partner_partnership_trading_name(formWithErrors, "Test User", backUrl))
          },
          tradingName => {
            //TODO [DLS-7603] - Todo cache tradingName data and fetch type of legal entity for the partner from save4later cache
            request.cookies.get("businessType").map(_.value) match {
              case Some(businessType) if businessType.equals("partnership") =>
                Redirect(routes.BusinessPartnersPartnershipVatNumberController.load())
              case Some(businessType) if businessType.equals("limited-liability-partnership") =>
                Redirect(routes.BusinessPartnersCorporateBodyCompanyRegNumberController.load())
              case Some(unexpectedBusinessType) =>
                logger.warn(
                  s"[BusinessPartnerPartnershipTradingNameController][next]: Unexpected error, $unexpectedBusinessType refreshing the page ")
                Redirect(routes.BusinessPartnerPartnershipTradingNameController.load().url).discardingCookies()
              case _ =>
                logger.error(
                  s"[BusinessPartnerPartnershipTradingNameController][next]: Unknown exception, returning $INTERNAL_SERVER_ERROR")
                errorHandler.errorResultsPages(Results.InternalServerError)
            }
          }
        ) // TODO [DLS-7603] - temp save4later solution remove when cookies removed from load function
    } else {
      errorHandler
        .errorResultsPages(Results.NotFound) // TODO [DLS-7603] - temp save4later solution remove when cookies removed from load function
    }
  }

}
