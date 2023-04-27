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

class BusinessPartnerCorporateBodyRegisteredAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._
  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      // Todo get this from cache later
      val corporateBody = "Test Corporate Body"
      val bpAddressForm = businessPartnersAddressForm
      Ok(view.business_partners_corporateBody_registered_address(bpAddressForm, corporateBody))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      // Todo get this from cache later
      val corporateBody = "Test Corporate Body"
      businessPartnersAddressForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(view.business_partners_corporateBody_registered_address(formWithErrors, corporateBody))
        },
        bpAddress => {
          val addressLineMsg = bpAddress.addressLine match {
            case Some(addressLine) => s"address line $addressLine"
            case _                 => "no address line"
          }
          Ok(s"Next page! with postcode: ${bpAddress.postcode} and $addressLineMsg")
        }
      )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

}
