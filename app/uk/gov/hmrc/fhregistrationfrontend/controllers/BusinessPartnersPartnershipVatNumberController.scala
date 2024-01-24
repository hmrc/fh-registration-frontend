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
import models.Mode
import models.NormalMode
import play.api.data.Form
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.VatNumberForm.vatNumberForm
import uk.gov.hmrc.fhregistrationfrontend.forms.models.VatNumber
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.EnterVatNumberPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessPartnersPartnershipVatNumberController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._

  val form: Form[VatNumber] = vatNumberForm
  val partnerName: String = "Test Partner"
  val businessPartnerType: String = "partnership"

  def postAction(index: Int, mode: Mode): Call = routes.BusinessPartnersPartnershipVatNumberController.next(index, mode)

  def getBusinessType: String = config.getRandomBusinessType()

  val backUrl: String = {
    if (getBusinessType == "partnership")
      routes.BusinessPartnersPartnershipTradingNameController.load(index = 1, NormalMode).url
    else if (getBusinessType == "limited-liability-partnership")
      routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load(1, NormalMode).url
    else
      "#"
  }

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction { implicit request =>
    val formData = request.userAnswers.get(EnterVatNumberPage(index))
    val prepopulatedForm = formData.map(data => form.fill(data)).getOrElse(form)

    Ok(
      view.business_partners_has_vat_number(
        prepopulatedForm,
        businessPartnerType,
        partnerName,
        postAction(index, mode),
        backUrl
      )
    ).withCookies(Cookie("businessType", getBusinessType))
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            BadRequest(
              view.business_partners_has_vat_number(
                formWithErrors,
                businessPartnerType,
                partnerName,
                postAction(index, mode),
                backUrl
              )
            )
          )
        },
        vatNumber => {
          val page = EnterVatNumberPage(index)
          val nextPage = request.cookies.get("businessType").map(_.value) match {
            case Some(businessType)
                if businessType.equals("partnership") || (businessType
                  .equals("limited-liability-partnership") && vatNumber.value.isEmpty) =>
              routes.BusinessPartnersPartnershipUtrController.load()
            case Some(businessType) if businessType.equals("limited-liability-partnership") && vatNumber.hasValue =>
              routes.BusinessPartnersPartnershipRegisteredAddressController.load(index, mode)
          }

          val updatedUserAnswers = request.userAnswers.set(page, vatNumber)
          updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, page)
        }
      )
  }
}
