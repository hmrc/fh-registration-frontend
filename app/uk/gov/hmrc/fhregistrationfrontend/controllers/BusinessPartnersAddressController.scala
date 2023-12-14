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

import play.api.data.FormError
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.connectors.AddressLookupErrorResponse
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersAddressForm.{businessPartnersAddressForm, postcodeKey}
import uk.gov.hmrc.fhregistrationfrontend.services.AddressService
import models.{Mode, NormalMode, UserAnswers}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{PartnerName, UkAddressLookup}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.BusinessPartnerAddressPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnerAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  addressService: AddressService,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {

  import actions._
  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction { implicit request =>
    val formData = request.userAnswers.get(BusinessPartnerAddressPage(index))
    val prepopulatedForm =
      formData.map(data => businessPartnersAddressForm.fill(data)).getOrElse(businessPartnersAddressForm)
    val partnerName = "Test User"
    Ok(view.business_partners_search_address(prepopulatedForm, partnerName))
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction.async { implicit request =>
    val partnerName = "Test User"
    val addressLookup = UkAddressLookup(Some("testline"), "wn75lg")
    businessPartnersAddressForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            BadRequest(view.business_partners_search_address(formWithErrors, partnerName))
          )
        },
        bpAddress => {
          val page = BusinessPartnerAddressPage(index)
          addressService
            .addressLookup(
              routes.BusinessPartnersAddressController.load(1, NormalMode).path(),
              bpAddress.postcode,
              bpAddress.addressLine
            )
            .flatMap {
              case Right(addressListMap) =>
                val nextPage =
                  if (addressListMap.isEmpty)
                    routes.BusinessPartnersCannotFindAddressController.load()
                  else if (addressListMap.size == 1)
                    routes.BusinessPartnersConfirmAddressController.load()
                  else
                    routes.BusinessPartnersChooseAddressController.load()

                val userAnswers = request.userAnswers
                val updatedUserAnswers = userAnswers.set[UkAddressLookup](page, addressLookup)
                updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, page)

              case Left(AddressLookupErrorResponse(_)) =>
                val formWithErrors = businessPartnersAddressForm
                  .fill(bpAddress)
                  .withError(FormError(postcodeKey, "address.lookup.error"))
                Future.successful(BadRequest(view.business_partners_search_address(formWithErrors, partnerName)))
              case _ => Future.successful(Redirect(routes.BusinessPartnersController.load()))
            }
        }
      )
  }

}
