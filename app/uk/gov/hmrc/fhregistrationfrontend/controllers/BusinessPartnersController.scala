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

import models.{CheckMode, Mode, NormalMode, UserAnswers}
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnerTypeForm.businessPartnerTypeForm
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerType
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnerTypePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessPartnersController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  val sessionCache: SessionRepository,
  cc: MessagesControllerComponents)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  def postAction(index: Int, mode: Mode): Call = routes.BusinessPartnersController.next(index, mode)
  val backUrl: String = routes.ContactPersonController.load().url

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRetrievalAction { implicit request =>
    val optPreviousSelectedAnswers = {
      request.optUserAnswers.fold[Option[BusinessPartnerType.Value]](None)(_.get(PartnerTypePage(index)))
    }
    val prepopulatedForm = optPreviousSelectedAnswers match {
      case Some(data) => businessPartnerTypeForm().fill(data)
      case None       => businessPartnerTypeForm()
    }
    Ok(view.business_partners_type(prepopulatedForm, "first", postAction(index, mode), backUrl))
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRetrievalAction.async { implicit request =>
    businessPartnerTypeForm().bindFromRequest.fold(
      formWithErrors => {
        Future.successful(
          BadRequest(view.business_partners_type(formWithErrors, "first", postAction(index, mode), backUrl))
        )
      },
      businessType => {
        val nextUrl = businessType match {
          case BusinessPartnerType.UnincorporatedBody =>
            routes.BusinessPartnersUnincorporatedBodyNameController.load()
          case BusinessPartnerType.Partnership =>
            routes.BusinessPartnersPartnershipNameController.load(index, NormalMode)
          case BusinessPartnerType.LimitedLiabilityPartnership =>
            routes.BusinessPartnersLtdLiabilityPartnershipNameController.load()
          case BusinessPartnerType.CorporateBody =>
            routes.BusinessPartnersCorporateBodyCompanyNameController.load()
          case _ =>
            routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.load(index, NormalMode)
        }
        val optPreviousSelectedAnswers =
          request.optUserAnswers.fold[Option[BusinessPartnerType.Value]](None)(_.get(PartnerTypePage(index)))
        optPreviousSelectedAnswers match {
          case Some(answer) if answer == businessType && mode == CheckMode =>
            Future.successful(Redirect(routes.BusinessPartnersCheckYourAnswersController.load()))
          case Some(answer) if answer == businessType =>
            Future.successful(Redirect(nextUrl))
          case _ =>
            val newUserAnswers = UserAnswers(request.userId).set(PartnerTypePage(index), businessType)
            updateUserAnswersAndSaveToCache(newUserAnswers, nextUrl, PartnerTypePage(index))
        }
      }
    )
  }
}
