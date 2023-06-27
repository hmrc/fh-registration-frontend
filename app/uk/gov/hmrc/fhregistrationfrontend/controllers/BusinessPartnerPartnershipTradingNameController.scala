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
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessPartnerPartnershipTradingNameController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  save4LaterService: Save4LaterService)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(view.business_partner_partnership_trading_name(tradingNameForm, "Test User"))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction.async { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      tradingNameForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(view.business_partner_partnership_trading_name(formWithErrors, "Test User")))
        },
        tradingName => {
          //Todo cache tradingName data
          //fetch data from save4later

          // businessType match {
          // case "partnership" => redirect to BusinessPartnersPartnershipVatNumberController.load
          // case "ltd partnership" => redirect to BusinessPartnersCorporateBodyCompanyRegNumberController.load
          // case _ => log error with message s"Could not parse businessType $businessType" then redirect to errorHandler.errorResultsPages(Results.BadRequest)

          save4LaterService
            .fetchBusinessType(request.userId)
            .map {
              case Some(businessType) if businessType.equals("Partnership") =>
                println(Console.MAGENTA + s"Business Type: $businessType" + Console.RESET)
                logger.info(s"Business Type: $businessType")
                Redirect(routes.BusinessPartnersPartnershipVatNumberController.load())
              // There is no "ltd partnership" option in businessType enum object, so this will never be hit
              case Some(businessType) if businessType.equals("ltd partnership") =>
                println(Console.MAGENTA + s"Business Type: $businessType" + Console.RESET)
                Redirect(routes.BusinessPartnersCorporateBodyCompanyRegNumberController.load())
              case Some(businessType) =>
                println(Console.MAGENTA + s"Business Type: $businessType" + Console.RESET)
                logger.warn(
                  s"[BusinessPartnerPartnershipTradingNameController][next] - Could not parse businessType $businessType")
                errorHandler.errorResultsPages(Results.BadRequest)
            }
            .recover {
              case _ =>
                logger.error(
                  s"[BusinessPartnerPartnershipTradingNameController][next][recover] - Failed to retrieve businessType from cache")
                errorHandler.errorResultsPages(Results.InternalServerError)
            }
        }
      )
    } else {
      Future.successful(errorHandler.errorResultsPages(Results.NotFound))
    }
  }

}
