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
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.TradingNameForm.tradingNameForm
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.{PartnershipTradingNamePage => page}
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnersPartnershipTradingNameController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  val backUrl: String = {
    if (getBusinessType == "partnership")
      routes.BusinessPartnersPartnershipNameController.load(1, NormalMode).url
    else if (getBusinessType == "limited-liability-partnership")
      routes.BusinessPartnersLtdLiabilityPartnershipNameController.load().url
    else "#"
  }
  val businessType = "partnership"
  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersPartnershipTradingNameController.next(index, mode)
  val partner = "Test User"

  private def getBusinessType: String = config.getRandomBusinessType

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction { implicit request =>
    val formData = request.userAnswers.get(page(index))
    val prepopulatedForm = formData.map(data => tradingNameForm.fill(data)).getOrElse(tradingNameForm)

    Ok(
      view
        .business_partners_has_trading_name(prepopulatedForm, businessType, partner, postAction(index, mode), backUrl))
      .withCookies(Cookie("businessType", getBusinessType))
      .bakeCookies() // TODO [DLS-7603] - temp save4later solution
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction.async { implicit request =>
    tradingNameForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            BadRequest(
              view.business_partners_has_trading_name(
                formWithErrors,
                businessType,
                partner,
                postAction(index, mode),
                backUrl)))
        },
        tradingName => {
          val nextPage = request.cookies.get("businessType").map(_.value) match {
            case Some(businessType) if businessType.equals("partnership") =>
              routes.BusinessPartnersPartnershipVatNumberController.load()
            case Some(businessType) if businessType.equals("limited-liability-partnership") =>
              routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load()
            case _ =>
              logger.warn(
                s"[BusinessPartnerPartnershipTradingNameController][next]: Unexpected error, redirecting to start of journey")
              routes.BusinessPartnersController.load()
          }

          val updatedUserAnswers = request.userAnswers.set(page(index), tradingName)
          updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, page(index))
        }
      ) // TODO [DLS-7603] - temp save4later solution remove when cookies removed from load function
  }

}
