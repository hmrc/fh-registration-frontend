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
import play.api.data.Form
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.controllers.{AppController, CommonPlayDependencies, ControllerHelper}
import uk.gov.hmrc.fhregistrationfrontend.forms.businessPartners.LtdLiabilityPartnershipNameForm.{ltdLiabilityPartnershipNameForm, ltdLiabilityPartnershipNameKey}
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.LtdLiabilityPartnershipName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.LimitedLiabilityPartnershipNamePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnersLtdLiabilityPartnershipNameController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  val businessPartnerType: String = "ltdLiabilityPartnership"

  def backAction(index: Int, mode: Mode): String =
    routes.BusinessPartnersController.load(index, mode).url

  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersLtdLiabilityPartnershipNameController.next(index, mode)

  lazy val form: Form[LtdLiabilityPartnershipName] = ltdLiabilityPartnershipNameForm

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode) {
    implicit request =>
      val formData = request.userAnswers.get(LimitedLiabilityPartnershipNamePage(index))
      val prepopulatedForm = formData.map(data => form.fill(data)).getOrElse(form)
      Ok(
        view.business_partners_enter_company_name(
          prepopulatedForm,
          ltdLiabilityPartnershipNameKey,
          businessPartnerType,
          postAction(index, mode),
          backAction(index, mode)
        )
      )
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode).async {
    implicit request =>
      ltdLiabilityPartnershipNameForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(
              BadRequest(
                view.business_partners_enter_company_name(
                  formWithErrors,
                  ltdLiabilityPartnershipNameKey,
                  businessPartnerType,
                  postAction(index, mode),
                  backAction(index, mode)
                )
              )
            )
          },
          ltdLiabilityPartnership => {
            val currentPage = LimitedLiabilityPartnershipNamePage(index)
            val nextPage = routes.BusinessPartnersPartnershipTradingNameController.load(index, mode)

            val updatedUserAnswers = request.userAnswers.set(currentPage, ltdLiabilityPartnership)
            updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, currentPage)
          }
        )
  }
}
