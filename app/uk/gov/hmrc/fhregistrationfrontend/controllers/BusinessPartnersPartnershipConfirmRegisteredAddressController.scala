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
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersChooseAddressForm.chooseAddressForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.{AddressPage, UkAddressLookupPage}
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessPartnersPartnershipConfirmRegisteredAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {
  import actions._

  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersPartnershipConfirmRegisteredAddressController.next(index)
  val journey = "partnership"
  val backLink = "#"
  val companyName = "company"
  val editAddressUrl: String = routes.BusinessPartnersPartnershipEnterAddressController.load().url

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction { implicit request =>
    val getUserAnswers = request.userAnswers.get(UkAddressLookupPage(index))
    val cachedAddressList = getUserAnswers.map(data => (data.lookupResult)).getOrElse(Map.empty)

    if (cachedAddressList.isEmpty || cachedAddressList.size > 1)
      Redirect(routes.BusinessPartnersPartnershipRegisteredAddressController.load(index, mode))
    else {
      val addressToConfirm = cachedAddressList.head._2
      Ok(
        view
          .business_partners_confirm_registered_address(
            addressToConfirm,
            companyName,
            journey,
            postAction(index, mode),
            backLink,
            editAddressUrl))
    }
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction.async { implicit request =>
    val getUserAnswers = request.userAnswers.get(UkAddressLookupPage(index))
    val cachedAddressList = getUserAnswers.map(data => (data.lookupResult)).getOrElse(Map.empty)

    if (cachedAddressList.isEmpty || cachedAddressList.size > 1) {
      Future.successful(Redirect(routes.BusinessPartnersPartnershipRegisteredAddressController.load(index, mode)))
    } else {
      val page = AddressPage(index)
      val nextPage = routes.BusinessPartnersCheckYourAnswersController.load()
      val updatedUserAnswers = request.userAnswers.set(page, cachedAddressList.head._2)

      updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, page)
    }
  }
}
