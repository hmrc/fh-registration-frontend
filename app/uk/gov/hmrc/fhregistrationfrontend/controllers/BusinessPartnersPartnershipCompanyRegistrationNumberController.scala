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
import models.{Mode, NormalMode}
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.CompanyRegistrationNumberForm.{companyRegistrationNumberForm => form}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.{CompanyRegistrationNumberPage => page}
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessPartnersPartnershipCompanyRegistrationNumberController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  val businessType: String = "limited-liability-partnership"
  val companyName: String = "Test Partnership"
  def backLink(index: Int, mode: Mode): String =
    routes.BusinessPartnersPartnershipTradingNameController.load(index, mode).url
  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.next(index, mode)

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode) {
    implicit request =>
      val formData = request.userAnswers.get(page(index))
      val prepopulatedForm = formData.map(data => form.fill(data)).getOrElse(form)

      Ok(
        view
          .business_partners_enter_crn(
            prepopulatedForm,
            companyName,
            businessType,
            postAction(index, mode),
            backLink(index, mode)))
        .withCookies(Cookie("businessType", businessType))
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(
              BadRequest(
                view
                  .business_partners_enter_crn(
                    formWithErrors,
                    companyName,
                    businessType,
                    postAction(index, mode),
                    backLink(index, mode)))
            )
          },
          regNumber => {
            val currentPage = page(index)
            val nextPage = routes.BusinessPartnersPartnershipVatNumberController.load(1, mode)
            val updatedUserAnswers = request.userAnswers.set(currentPage, regNumber)
            updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, currentPage)
          }
        )
  }
}
