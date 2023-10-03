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

import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.connectors.AddressLookupErrorResponse
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersAddressForm.{businessPartnersAddressForm, postcodeKey}
import uk.gov.hmrc.fhregistrationfrontend.services.AddressService
import play.api.data.FormError
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessPartnersCorporateBodyRegisteredAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  addressService: AddressService)(
  cc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends AppController(ds, cc) {

  // Todo get this from cache later
  val postAction: Call = Call(
    method = "POST",
    url = routes.BusinessPartnersCorporateBodyRegisteredAddressController
      .next()
      .url
  )
  val businessType = "corporateBody"
  val corporateBody = "Test Corporate Body"
  val hasVatNum: Boolean = config.hasVatNumber()
  val enterManualAddressUrl: String = routes.BusinessPartnersCorporateBodyEnterAddressController.load().url

  val backUrl: String = {
    if (hasVatNum) routes.BusinessPartnersUtrController.load().url
    else routes.BusinessPartnersCorporateBodyUtrController.load().url
  }

  import actions._
  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(
        view
          .business_partners_registered_address(
            businessPartnersAddressForm,
            corporateBody,
            backUrl,
            postAction,
            businessType,
            enterManualAddressUrl
          ))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction.async { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      businessPartnersAddressForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(
              BadRequest(
                view.business_partners_registered_address(
                  formWithErrors,
                  corporateBody,
                  backUrl,
                  postAction,
                  businessType,
                  enterManualAddressUrl
                )
              )
            )
          },
          bpAddress => {
            addressService
              .addressLookup(
                routes.BusinessPartnersCorporateBodyRegisteredAddressController.load().path(),
                bpAddress.postcode,
                bpAddress.addressLine
              )
              .map {
                case Right(addressListMap) =>
                  // ToDo store the addressListMap in cache
                  if (addressListMap.isEmpty)
                    Redirect(routes.BusinessPartnersCannotFindAddressController.load())
                  else if (addressListMap.size == 1)
                    Redirect(routes.BusinessPartnersConfirmCorporateRegisteredAddressController.load())
                  else
                    Redirect(routes.BusinessPartnersChooseAddressController.load())

                case Left(AddressLookupErrorResponse(_)) =>
                  val formWithErrors = businessPartnersAddressForm
                    .fill(bpAddress)
                    .withError(FormError(postcodeKey, "address.lookup.error"))
                  BadRequest(
                    view.business_partners_registered_address(
                      formWithErrors,
                      corporateBody,
                      backUrl,
                      postAction,
                      businessType,
                      enterManualAddressUrl
                    ))
                case _ => errorHandler.errorResultsPages(Results.InternalServerError)
              }
          }
        )
    } else {
      Future.successful(errorHandler.errorResultsPages(Results.NotFound))
    }
  }

}
