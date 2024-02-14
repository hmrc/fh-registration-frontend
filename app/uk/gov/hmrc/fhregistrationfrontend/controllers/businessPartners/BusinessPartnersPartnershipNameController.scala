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

import com.google.inject.{Inject, Singleton}
import models.Mode
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.controllers.{AppController, CommonPlayDependencies, ControllerHelper}
import uk.gov.hmrc.fhregistrationfrontend.forms.businessPartners.PartnershipNameForm.{partnershipNameForm, partnershipNameKey}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnershipNamePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessPartnersPartnershipNameController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {
  import actions._

  lazy val journeyType = "partnership"
  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersPartnershipNameController.next(index, mode)
  def backAction(index: Int, mode: Mode): String =
    routes.PartnerTypeController.load(index, mode).url
  def tradingNamePage(index: Int, mode: Mode): Call =
    routes.BusinessPartnersPartnershipTradingNameController.load(index, mode)
  lazy val form: Form[String] = partnershipNameForm

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode) {
    implicit request =>
      val currentPage = PartnershipNamePage(index)
      val formData = request.userAnswers.get(currentPage)
      val prepopulatedForm = formData.map(data => form.fill(data)).getOrElse(form)
      Ok(
        view
          .business_partners_name(
            journeyType,
            postAction(index, mode),
            prepopulatedForm,
            partnershipNameKey,
            backAction(index, mode)
          ))
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(
              BadRequest(
                view.business_partners_name(
                  journeyType,
                  postAction(index, mode),
                  formWithErrors,
                  partnershipNameKey,
                  backAction(index, mode)
                )
              )
            )
          },
          partnership => {
            val currentPage = PartnershipNamePage(index)
            val updatedUserAnswers = request.userAnswers.set(currentPage, partnership)
            updateUserAnswersAndSaveToCache(updatedUserAnswers, tradingNamePage(index, mode), currentPage)
          }
        )
  }
}
