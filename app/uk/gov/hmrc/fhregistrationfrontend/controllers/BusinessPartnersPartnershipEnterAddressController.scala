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
import models.NormalMode
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersEnterAddressForm.enterAddressForm
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.EnterAddressPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnersPartnershipEnterAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  val partnerName: String = "Test User"
  val journeyType: String = "enterRegisteredOfficeAddress"

  val backLink: String = routes.BusinessPartnersPartnershipRegisteredAddressController.load(1, NormalMode).url
  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersPartnershipEnterAddressController.next(index, mode)

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode) {
    implicit request =>
      val formData = request.userAnswers.get(EnterAddressPage(index))
      val prepopulatedForm = formData.map(data => enterAddressForm.fill(data)).getOrElse(enterAddressForm)

      Ok(
        view.business_partners_enter_address(
          prepopulatedForm,
          postAction(index, mode),
          partnerName,
          journeyType,
          backLink
        )
      )
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode).async {
    implicit request =>
      enterAddressForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(
              BadRequest(
                view.business_partners_enter_address(
                  formWithErrors,
                  postAction(index, mode),
                  partnerName,
                  journeyType,
                  backLink
                )
              )
            )
          },
          bpAddress => {
            val currentPage = EnterAddressPage(index)
            val nextPage = routes.BusinessPartnersCheckYourAnswersController.load()

            val updatedUserAnswers = request.userAnswers.set(currentPage, bpAddress)
            updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, currentPage)
          }
        )
  }

}
