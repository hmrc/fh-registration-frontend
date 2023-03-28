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
import play.api.data.FormError
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import play.filters.csrf.CSRF.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersChooseAddressForm.{chooseAddressForm, chooseAddressKey}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.fhregistrationfrontend.views.Views

@Singleton
class BusinessPartnersChooseAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {
  import actions._

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      val form = chooseAddressForm
      //ToDo read this data from the cache after being stored before the redirect
      val addressList = testAddressData
      Ok(view.business_partner_choose_address(form, addressList))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      //ToDo read this data from the cache after being stored before the redirect
      val addressList = testAddressData

      chooseAddressForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(view.business_partner_choose_address(formWithErrors, testAddressData))
        },
        addressKey => {
          addressList.get(addressKey.chosenAddress) match {
            case Some(address) => Ok(s"Next page! with form result: ${address.toString}")
            case None =>
              val formWithError =
                chooseAddressForm.withError(FormError("chosenAddress", "error.required"))
              BadRequest(view.business_partner_choose_address(formWithError, testAddressData))
          }
        }
      )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  //ToDo remove when addressData stored in database
  private val testAddressData: Map[String, Address] = {
    val address1 = Address(
      addressLine1 = "1 Romford Road",
      addressLine2 = Some("Wellington"),
      addressLine3 = Some("Telford"),
      addressLine4 = None,
      postcode = "TF1 4ER",
      countryCode = None,
      lookupId = None
    )

    val address2 = address1.copy(addressLine1 = "2 Romford Road")
    val address3 = address1.copy(addressLine1 = "3 Romford Road")
    val address4 = address1.copy(addressLine1 = "2 Romford Road")
    val address5 = address1.copy(addressLine1 = "5 Romford Road")

    Map(
      "1" -> address1,
      "2" -> address2,
      "3" -> address3,
      "4" -> address4,
      "5" -> address5
    )
  }

}
