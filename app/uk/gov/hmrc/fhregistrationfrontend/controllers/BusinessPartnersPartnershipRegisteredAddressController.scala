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

import models.Mode
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.connectors.AddressLookupErrorResponse
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersUkAddressLookupForm.{businessPartnersUkAddressLookupForm, postcodeKey}
import uk.gov.hmrc.fhregistrationfrontend.services.AddressService
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.data.FormError
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, UkAddressLookup}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.UkAddressLookupPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository

class BusinessPartnersPartnershipRegisteredAddressController @Inject()(
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

  // Todo get this from cache later

  private def getBusinessType: String = config.getRandomBusinessType()

  def backUrl(index: Int, mode: Mode): String =
    if (getBusinessType == "partnership")
      routes.BusinessPartnersPartnershipUtrController.load().url
    else if (getBusinessType == "limited-liability-partnership")
      routes.BusinessPartnersSoleProprietorsVatRegistrationNumberController.load(index, mode).url
    else
      "#"

  val partnerName = "Test User"
  val journey = "partnership"
  def postAction(index: Int, mode: Mode): Call =
    routes.BusinessPartnersPartnershipRegisteredAddressController.next(index, mode)
  def enterManualAddressUrl(index: Int, mode: Mode): String =
    routes.BusinessPartnersPartnershipEnterAddressController.load().url

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction { implicit request =>
    val formData: Option[UkAddressLookup] = request.userAnswers.get(UkAddressLookupPage(index))
    val prepopulatedForm = formData
      .map(addressData => businessPartnersUkAddressLookupForm.fill(addressData))
      .getOrElse(businessPartnersUkAddressLookupForm)
    Ok(
      view.business_partners_registered_address(
        prepopulatedForm,
        partnerName,
        backUrl(index, mode),
        postAction(index, mode),
        journey,
        enterManualAddressUrl(index, mode)
      )
    )
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction.async { implicit request =>
    businessPartnersUkAddressLookupForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            BadRequest(
              view.business_partners_registered_address(
                formWithErrors,
                partnerName,
                backUrl(index, mode),
                postAction(index, mode),
                journey,
                enterManualAddressUrl(index, mode)
              )
            )
          )
        },
        bpAddress => {
          val getUserAnswers = request.userAnswers.get(UkAddressLookupPage(index))
          val postcode = getUserAnswers.map(data => data.postcode).getOrElse("")
          val lookupResultAnswers = getUserAnswers.map(data => data.lookupResult).getOrElse(Map.empty)

          val addressLookupCall: Future[Either[AddressLookupErrorResponse, Map[String, Address]]] =
            if (postcode.equals(bpAddress.postcode)) {
              Future(Right(lookupResultAnswers))
            } else {
              addressService
                .addressLookup(
                  routes.BusinessPartnersAddressController.load(index, mode).path(),
                  bpAddress.postcode,
                  bpAddress.addressLine
                )
            }

          addressLookupCall
            .flatMap {
              case Right(addressListMap) =>
                val page = UkAddressLookupPage(index)
                val nextPage = addressListMap.size match {
                  case 0 => routes.BusinessPartnersCannotFindAddressController.load(index, mode)
                  case 1 => routes.BusinessPartnersPartnershipConfirmRegisteredAddressController.load()
                  case _ => routes.BusinessPartnersChooseAddressController.load(index, mode)
                }

                val lookupResult: UkAddressLookup =
                  UkAddressLookup(bpAddress.addressLine, bpAddress.postcode, addressListMap)
                val updatedUserAnswers = request.userAnswers.set(page, lookupResult)
                updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, page)

              case Left(AddressLookupErrorResponse(_)) =>
                val formWithErrors = businessPartnersUkAddressLookupForm
                  .fill(bpAddress)
                  .withError(FormError(postcodeKey, "address.lookup.error"))
                Future.successful(BadRequest(
                  view.business_partners_search_address(formWithErrors, partnerName, postAction(index, mode))))
              case _ => Future.successful(errorHandler.errorResultsPages(Results.InternalServerError))
            }
        }
      )
  }

}
