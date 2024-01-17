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
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersEnterUtrForm.businessPartnersEnterUtrForm
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.SoleProprietorUtrPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BusinessPartnersSoleProprietorUtrController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  val partnerName: String = "{{partner name}}"
  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersSoleProprietorUtrController.next(index: Int, mode: Mode)
  val businessPartnerType: String = "SoleProprietor"
  def backUrl(index: Int, mode: Mode): String =
    routes.BusinessPartnersVatRegistrationNumberController.load().url

  def load(index: Int, mode: Mode): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(
        view.business_partners_enter_utr_number(
          businessPartnersEnterUtrForm,
          partnerName,
          businessPartnerType,
          postAction(index, mode),
          backUrl(index, mode)))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction.async { implicit request =>
    businessPartnersEnterUtrForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            BadRequest(
              view.business_partners_enter_utr_number(
                formWithErrors,
                partnerName,
                businessPartnerType,
                postAction(index, mode),
                backUrl(index, mode))
            ))
        },
        value => {
          val updatedUserAnswers = request.userAnswers.copy(utr = Some(value))
          val nextPage = routes.BusinessPartnersAddressController.load(index, mode)
          updateUserAnswersAndSaveToCache(Try(updatedUserAnswers), nextPage, SoleProprietorUtrPage(index))
        }
      )
  }
}
