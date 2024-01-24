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

import com.google.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.VatNumberForm.vatNumberForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import models.{Mode, NormalMode}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.EnterVatNumberPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessPartnersSoleProprietorsVatRegistrationNumberController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {
  import actions._

  val partnerName: String = "test partner"
  val backUrl: String = routes.BusinessPartnersIndividualsAndSoleProprietorsNinoController.load(1, NormalMode).url
  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersSoleProprietorsVatRegistrationNumberController.next(index, mode)

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction(index, mode) { implicit request =>
    val formData = request.userAnswers.get(EnterVatNumberPage(index))
    val prepopulatedForm = formData.map(data => vatNumberForm.fill(data)).getOrElse(vatNumberForm)
    Ok(
      view.business_partners_enter_vat_registration(
        prepopulatedForm,
        postAction(index, mode),
        partnerName,
        backUrl
      )
    )
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction(index, mode).async { implicit request =>
    vatNumberForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            BadRequest(
              view.business_partners_enter_vat_registration(
                formWithErrors,
                postAction(index, mode),
                partnerName,
                backUrl
              )
            )
          )
        },
        vatNumber => {
          val page = EnterVatNumberPage(index)
          val nextPage = vatNumber.value match {
            case Some(vatNumber) => routes.BusinessPartnersAddressController.load(1, mode)
            case None            => routes.BusinessPartnersSoleProprietorUtrController.load(index, mode)
          }

          val updatedUserAnswers = request.userAnswers.set(page, vatNumber)
          updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, page)
        }
      )
  }
}
