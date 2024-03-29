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

package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import models.Mode
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.TradingNameForm.{tradingNameForm => form}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.{PartnershipTradingNamePage => page}
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.fhregistrationfrontend.controllers.{AppController, CommonPlayDependencies, ControllerHelper}

class BusinessPartnersPartnershipTradingNameController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  def backUrl(index: Int, mode: Mode): String =
    if (getBusinessType == "partnership")
      routes.BusinessPartnersPartnershipNameController.load(index, mode).url
    else if (getBusinessType == "limited-liability-partnership")
      routes.BusinessPartnersLtdLiabilityPartnershipNameController.load(index, mode).url
    else "#"
  val businessType = "partnership"
  def postAction(index: Int, mode: Mode) =
    routes.BusinessPartnersPartnershipTradingNameController.next(index, mode)
  val partner = "Test User"

  private def getBusinessType: String = config.getRandomBusinessType

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode) {
    implicit request =>
      val currentPage = page(index)
      val formData = request.userAnswers.get(currentPage)
      val prepopulatedForm = formData.map(data => form.fill(data)).getOrElse(form)

      Ok(
        view
          .business_partners_has_trading_name(
            prepopulatedForm,
            businessType,
            partner,
            postAction(index, mode),
            backUrl(index, mode)))
        .withCookies(Cookie("businessType", getBusinessType))
        .bakeCookies() // TODO [DLS-7603] - temp save4later solution
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode).async {
    implicit request =>
      form
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
                  backUrl(index, mode))))
          },
          tradingName => {
            val nextPage = request.cookies.get("businessType").map(_.value) match {
              case Some(businessType) if businessType.equals("partnership") =>
                routes.BusinessPartnersPartnershipVatNumberController.load(index, mode)
              case Some(businessType) if businessType.equals("limited-liability-partnership") =>
                routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load(index, mode)
              case _ =>
                logger.warn(
                  s"[BusinessPartnerPartnershipTradingNameController][next]: Unexpected error, redirecting to start of journey")
                routes.PartnerTypeController.load(index, mode)
            }

            val currentPage = page(index)
            val updatedUserAnswers = request.userAnswers.set(currentPage, tradingName)
            updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, currentPage)
          }
        ) // TODO [DLS-7603] - temp save4later solution remove when cookies removed from load function
  }

}
