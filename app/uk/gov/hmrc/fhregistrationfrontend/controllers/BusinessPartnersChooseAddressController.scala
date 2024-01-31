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
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersChooseAddressForm.chooseAddressForm
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ChooseAddress
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.{AddressPage, UkAddressLookupPage}
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import java.lang.System.console
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessPartnersChooseAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {
  import actions._

  private def getBusinessType: String = config.getRandomBusinessType()

  val backUrl: String = {
    if (getBusinessType == "unincorporated-body")
      routes.BusinessPartnersUnincorporatedBodyRegisteredAddressController.load().url
    else "#"
  }

  def postAction(index: Int, mode: Mode): Call = routes.BusinessPartnersChooseAddressController.next(index, mode)

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode) {
    implicit request =>
      val getUserAnswers = request.userAnswers.get(UkAddressLookupPage(index))
      val cachedAddressList = getUserAnswers.map(data => (data.lookupResult)).getOrElse(Map.empty)

      if (cachedAddressList.size <= 1) Redirect(routes.BusinessPartnersAddressController.load(index, mode))
      else {
        val formData = request.userAnswers.get(AddressPage(index))
        val prepopulatedForm =
          formData
            .flatMap { data =>
              cachedAddressList
                .find(_._2 == data)
                .map(addressPair => chooseAddressForm.fill(ChooseAddress(addressPair._1)))
            }
            .getOrElse(chooseAddressForm)

        Ok(
          view.business_partners_choose_address(
            prepopulatedForm,
            postAction(index, mode),
            cachedAddressList,
            backUrl
          )
        )
      }
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredActionBusinessPartners(index, mode).async {
    implicit request =>
      val getUserAnswers = request.userAnswers.get(UkAddressLookupPage(index))
      val cachedAddressList = getUserAnswers.map(data => (data.lookupResult)).getOrElse(Map.empty)

      if (cachedAddressList.size <= 1)
        Future.successful(Redirect(routes.BusinessPartnersAddressController.load(index, mode)))
      else {
        chooseAddressForm
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future.successful(
                BadRequest(
                  view.business_partners_choose_address(
                    formWithErrors,
                    postAction(index, mode),
                    cachedAddressList,
                    backUrl
                  )
                )
              )
            },
            addressKey => {
              val page = AddressPage(index)
              val nextPage = routes.BusinessPartnersCheckYourAnswersController.load()

              val updatedUserAnswers = request.userAnswers.set(page, cachedAddressList(addressKey.chosenAddress))
              updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, page)
            }
          )
      }
  }
}
