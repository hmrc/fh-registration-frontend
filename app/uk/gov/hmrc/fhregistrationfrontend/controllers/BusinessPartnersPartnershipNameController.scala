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
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.PartnershipNameForm.{partnershipNameForm, partnershipNameKey}
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
  def postAction(index: Int, mode: Mode): Call = routes.BusinessPartnersPartnershipNameController.next(index, mode)
  lazy val backAction: String = routes.BusinessPartnersController.load().url
  lazy val tradingNamePage: Call = routes.BusinessPartnersPartnershipTradingNameController.load(1, NormalMode)
  lazy val form: Form[String] = partnershipNameForm

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction { implicit request =>
    val formData = request.userAnswers.get(PartnershipNamePage(index))
    val prepopulatedForm = formData.map(data => form.fill(data)).getOrElse(form)
    Ok(
      view
        .business_partners_name(journeyType, postAction(index, mode), prepopulatedForm, partnershipNameKey, backAction))
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction.async { implicit request =>
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
                backAction
              )
            )
          )
        },
        partnership => {
          val page = PartnershipNamePage(index)
          val updatedUserAnswers = request.userAnswers.set(page, partnership)
          updateUserAnswersAndSaveToCache(updatedUserAnswers, tradingNamePage, page)
        }
      )
  }
}
