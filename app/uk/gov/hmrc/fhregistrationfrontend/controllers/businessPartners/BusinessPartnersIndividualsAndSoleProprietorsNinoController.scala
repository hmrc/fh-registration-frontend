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

import models.{Mode, NormalMode}
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.controllers.{AppController, CommonPlayDependencies, ControllerHelper}
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.NationalInsuranceNumberForm.nationalInsuranceNumberForm
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.IndividualsAndSoleProprietorsNinoPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RadioHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnersIndividualsAndSoleProprietorsNinoController @Inject()(
  radioHelper: RadioHelper,
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersIndividualsAndSoleProprietorsNinoController.next(index, mode)
  private def getBusinessType: String = config.getRandomBusinessType()

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode) {
    implicit request =>
      val formData = request.userAnswers.get(IndividualsAndSoleProprietorsNinoPage(index))
      val prepopulatedForm =
        formData.map(data => nationalInsuranceNumberForm.fill(data)).getOrElse(nationalInsuranceNumberForm)
      val items = radioHelper.conditionalYesNoRadio(prepopulatedForm)

      Ok(view.business_partners_has_nino(prepopulatedForm, items, postAction(index, mode)))
        .withCookies(Cookie("businessType", getBusinessType))
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode).async {
    implicit request =>
      nationalInsuranceNumberForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val items = radioHelper.conditionalYesNoRadio(formWithErrors)
            Future.successful(
              BadRequest(view.business_partners_has_nino(formWithErrors, items, postAction(index, mode)))
            )
          },
          nino => {
            val page = IndividualsAndSoleProprietorsNinoPage(index)
            val ninoForIndividual = "AB123456C"
            val nextPage = request.cookies.get("businessType").map(_.value) match {

              case Some(businessType) if businessType.equals("individual") && nino.value.contains(ninoForIndividual) =>
                routes.BusinessPartnersAddressController.load(index, mode)
              case Some(businessType) if businessType.equals("individual") =>
                routes.BusinessPartnersSoleProprietorsVatRegistrationNumberController
                  .load(1, NormalMode)
              case _ => routes.PartnerTypeController.load(index, mode)
            }

            val updatedUserAnswers = request.userAnswers.set(page, nino)
            updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, page)
          }
        )
  }

}
