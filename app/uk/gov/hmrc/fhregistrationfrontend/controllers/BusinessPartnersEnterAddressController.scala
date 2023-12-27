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
import uk.gov.hmrc.fhregistrationfrontend.config.{ErrorHandler, FrontendAppConfig}
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersEnterAddressForm.chooseAddressForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import models.{Mode, NormalMode, UserAnswers}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnersEnterAddress
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.EnterAddressPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnersEnterAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  val partnerName: String = "Test User"
  val journeyType: String = "enterAddress"
  val backUrl: String = routes.BusinessPartnersAddressController.load().url
  def postAction(index: Int, mode: Mode): Call = routes.BusinessPartnersEnterAddressController.next(index, mode)

  import actions._
  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction { implicit request =>
    val formData = request.userAnswers.get(EnterAddressPage(index))
    val prepopulatedForm = formData.map(data => chooseAddressForm.fill(data)).getOrElse(chooseAddressForm)
    Ok(
      view
        .business_partners_enter_address(
          prepopulatedForm,
          postAction(index, mode),
          partnerName,
          journeyType,
          backUrl
        )
    )
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction.async { implicit request =>
    chooseAddressForm
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
                backUrl)))
        },
        bpAddress => {
          val page = EnterAddressPage(index)
          val nextPage = routes.BusinessPartnersCheckYourAnswersController.load()

          val updatedUserAnswers = request.userAnswers.set(page, bpAddress)
          updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, page)
        }
      )
  }
}
