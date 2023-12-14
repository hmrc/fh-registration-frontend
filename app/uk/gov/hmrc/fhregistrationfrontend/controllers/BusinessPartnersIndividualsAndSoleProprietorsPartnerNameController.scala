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

import models.{Mode, NormalMode}
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.PartnerNameForm.{partnerNameForm => form}
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject

class BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._

  private def getBusinessType: String = config.getRandomBusinessType()

  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.next(index, mode)

  val backUrl: String = routes.BusinessPartnersController.load().url

  def load(index: Int, mode: Mode): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(view.business_partners_individualsAndSoleProprietors_partner_name(form, postAction(index, mode), backUrl))
        .withCookies(Cookie("businessType", getBusinessType))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(
              view.business_partners_individualsAndSoleProprietors_partner_name(
                formWithErrors,
                postAction(index, mode),
                backUrl))
          },
          partnerName => {
            request.cookies.get("businessType").map(_.value) match {
              case Some(businessType) if businessType.equals("individual") =>
                Redirect(routes.BusinessPartnerNinoController.load())
              case Some(businessType) if businessType.equals("sole-proprietor") =>
                Redirect(routes.BusinessPartnerTradingNameController.load())
              case _ =>
                Ok(s"Form submitted, with result: $partnerName")
            }
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
}
