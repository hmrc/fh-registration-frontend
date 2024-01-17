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

import models.Mode
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.TradingNameForm.tradingNameForm
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.SoleProprietorsTradingNamePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnersSoleProprietorsTradingNameController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  def backUrl(index: Int, mode: Mode): String =
    routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.load(index, mode).url
  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersSoleProprietorsTradingNameController.next(index, mode)

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction { implicit request =>
    val formData = request.userAnswers.get(SoleProprietorsTradingNamePage(index))
    val prepopulatedForm = formData.map(data => tradingNameForm.fill(data)).getOrElse(tradingNameForm)
    Ok(
      view.business_partners_has_trading_partner_name(
        prepopulatedForm,
        "Test User",
        postAction(index, mode),
        backUrl(index, mode)))
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction.async { implicit request =>
    tradingNameForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            BadRequest(
              view.business_partners_has_trading_partner_name(
                formWithErrors,
                "Test User",
                postAction(index, mode),
                backUrl(index, mode))))
        },
        tradingName => {
          val updatedUserAnswers = request.userAnswers.set(SoleProprietorsTradingNamePage(index), tradingName)
          val nextPage = routes.BusinessPartnersIndividualsAndSoleProprietorsNinoController.load(index, mode)
          updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, SoleProprietorsTradingNamePage(index))
        }
      )
  }

}
