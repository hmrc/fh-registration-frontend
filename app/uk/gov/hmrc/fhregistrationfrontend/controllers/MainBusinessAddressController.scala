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
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.UnusedMainBusinessAddressForm.mainBusinessAddressForm
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation.noNavigation
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.Address
import javax.inject.Inject

class MainBusinessAddressController @Inject() (
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig
)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(view.main_business_address(mainBusinessAddressForm, businessRegistrationDetails, noNavigation, postAction))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      mainBusinessAddressForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            BadRequest(
              view.main_business_address(formWithErrors, businessRegistrationDetails, noNavigation, postAction)
            ),
          mainBusinessAddress => Ok(s"Form submitted, with result:$mainBusinessAddress")
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  val address: Address = Address(
    line1 = "1 Test Road",
    line2 = "Testington",
    line3 = None,
    line4 = Some("Test ville"),
    postcode = Some("AB1 XYZ"),
    country = "United Kingdom"
  )
  val postAction: Call = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.MainBusinessAddressController.next()
  val businessRegistrationDetails: BusinessRegistrationDetails = BusinessRegistrationDetails(None, None, address, None)
}
