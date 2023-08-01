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
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersAddressForm.businessPartnersAddressForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject
import scala.util.Random

class BusinessPartnerPartnershipRegisteredAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {
  import actions._

  // Todo get this from cache later

  private def getBusinessType: String = config.getRandomBusinessType()

  val backUrl: String = {
    if (getBusinessType == "partnership")
      routes.BusinessPartnerUtrController.load().url
    else
      routes.BusinessPartnersVatRegistrationNumberController.load().url
  }

  val partnerName = "Test User"
  val journey = "partnership"
  val postAction: Call = Call(
    method = "POST",
    url = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.BusinessPartnerPartnershipRegisteredAddressController
      .next()
      .url
  )

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(
        view
          .business_partner_registered_address(businessPartnersAddressForm, partnerName, backUrl, postAction, journey))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      businessPartnersAddressForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(
              view.business_partner_registered_address(
                formWithErrors,
                partnerName,
                backUrl,
                postAction,
                journey
              ))
          },
          bpAddress => {
            // Todo implement address lookup
            if (bpAddress.addressLine.contains("1 Romford Road") && bpAddress.postcode.contains("TF1 4ER")) {
              Redirect(routes.BusinessPartnersConfirmPartnershipRegisteredAddressController.load())
            } else if (bpAddress.postcode.contains("HR33 7GP")) {
              Redirect(routes.BusinessPartnersCannotFindAddressController.load())
            } else {
              Redirect(routes.BusinessPartnersChooseAddressController.load())
            }
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

}
