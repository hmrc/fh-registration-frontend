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
import uk.gov.hmrc.fhregistrationfrontend.controllers.{AppController, CommonPlayDependencies, ControllerHelper}
import uk.gov.hmrc.fhregistrationfrontend.forms.businessPartners.PartnerNameForm.{partnerNameForm => form}
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.BusinessPartnerType
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.{IndividualsAndSoleProprietorsPartnerNamePage, PartnerTypePage}
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  private def getBusinessType: String = config.getRandomBusinessType()

  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.next(index, mode)

  def backUrl(index: Int, mode: Mode): String = routes.PartnerTypeController.load(index, mode).url

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode) {
    implicit request =>
      val formData = request.userAnswers.get(IndividualsAndSoleProprietorsPartnerNamePage(index))
      val prepopulatedForm = formData.map(data => form.fill(data)).getOrElse(form)
      Ok(
        view
          .business_partners_individualsAndSoleProprietors_partner_name(
            prepopulatedForm,
            postAction(index, mode),
            backUrl(index, mode)))
        .withCookies(Cookie("businessType", getBusinessType))
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(
              BadRequest(
                view.business_partners_individualsAndSoleProprietors_partner_name(
                  formWithErrors,
                  postAction(index, mode),
                  backUrl(index, mode))))
          },
          partnerName => {
            val page = IndividualsAndSoleProprietorsPartnerNamePage(index)
            val nextPage = request.userAnswers.get(PartnerTypePage(index)) match {
              case Some(businessType) if businessType.equals(BusinessPartnerType.Individual) =>
                routes.BusinessPartnersIndividualsAndSoleProprietorsNinoController.load(index, mode)
              case Some(businessType) if businessType.equals(BusinessPartnerType.SoleProprietor) =>
                routes.BusinessPartnersSoleProprietorsTradingNameController.load(index, mode)
              case _ => routes.PartnerTypeController.load(index, mode)
            }
            val updatedUserAnswers = request.userAnswers.set(page, partnerName)
            updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, page)
          }
        )
  }
}
