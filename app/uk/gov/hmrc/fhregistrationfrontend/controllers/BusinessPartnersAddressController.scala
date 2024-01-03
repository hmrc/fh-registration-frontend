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
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import models.NormalMode

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnersAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  addressService: AddressService)(
  cc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends AppController(ds, cc) {

  import actions._
  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      // Todo get this from cache later
      val partnerName = "Test User"
      val bpAddressForm = businessPartnersAddressForm
      Ok(view.business_partners_search_address(bpAddressForm, partnerName))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction.async { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      // Todo get this from cache later
      val partnerName = "Test User"
      businessPartnersAddressForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(
              BadRequest(view.business_partners_search_address(formWithErrors, partnerName))
            )
          },
          bpAddress => {
            addressService
              .addressLookup(
                routes.BusinessPartnersAddressController.load().path(),
                bpAddress.postcode,
                bpAddress.addressLine
              )
              .map {
                case Right(addressListMap) =>
                  //ToDo store the addressListMap in save4Later
                  if (addressListMap.isEmpty)
                    Redirect(routes.BusinessPartnersCannotFindAddressController.load())
                  else if (addressListMap.size == 1)
                    //TODO change the hard coded values when index and mode added to function params
                    Redirect(routes.BusinessPartnersConfirmAddressController.load(1, NormalMode))
                  else
                    Redirect(routes.BusinessPartnersChooseAddressController.load(1, NormalMode))
                case Left(AddressLookupErrorResponse(_)) =>
                  val formWithErrors = businessPartnersAddressForm
                    .fill(bpAddress)
                    .withError(FormError(postcodeKey, "address.lookup.error"))
                  BadRequest(view.business_partners_search_address(formWithErrors, partnerName))
                case _ => errorHandler.errorResultsPages(Results.InternalServerError)
              }
          }
        )
    } else {
      Future.successful(errorHandler.errorResultsPages(Results.NotFound))
    }
  }

}
