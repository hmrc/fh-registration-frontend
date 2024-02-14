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
import uk.gov.hmrc.fhregistrationfrontend.controllers.{AppController, CommonPlayDependencies, ControllerHelper}
import uk.gov.hmrc.fhregistrationfrontend.forms.businessPartners.BusinessPartnersUnincorporatedBodyNameForm.{unincorporatedBodyNameKey, unincorporatedBodyNameForm => form}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.{UnincorporatedBodyNamePage => page}
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnersUnincorporatedBodyNameController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  def backUrl(index: Int, mode: Mode): String = routes.PartnerTypeController.load(index, mode).url

  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersUnincorporatedBodyNameController.next(index, mode)
  val businessPartnerType = "unincorporatedBody"

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode) {
    implicit request =>
      val currentPage = page(index)
      val formData = request.userAnswers.get(currentPage)
      val prepopulatedForm = formData.map(data => form.fill(data)).getOrElse(form)
      Ok(
        view.business_partners_enter_company_name(
          prepopulatedForm,
          unincorporatedBodyNameKey,
          businessPartnerType,
          postAction(index, mode),
          backUrl(index, mode)))
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(
              BadRequest(
                view.business_partners_enter_company_name(
                  formWithErrors,
                  unincorporatedBodyNameKey,
                  businessPartnerType,
                  postAction(index, mode),
                  backUrl(index, mode)))
            )
          },
          unincorporatedBodyName => {
            val currentPage = page(index)
            val nextPage = routes.BusinessPartnersUnincorporatedBodyTradingNameController.load()
            val updatedUserAnswers = request.userAnswers.set(currentPage, unincorporatedBodyName)
            updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, currentPage)
          }
        )
  }

}
