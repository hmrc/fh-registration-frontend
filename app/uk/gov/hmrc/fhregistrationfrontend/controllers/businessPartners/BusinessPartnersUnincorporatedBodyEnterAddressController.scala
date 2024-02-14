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

package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import play.api.data.Form
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.controllers.{AppController, CommonPlayDependencies}
import uk.gov.hmrc.fhregistrationfrontend.forms.businessPartners.BusinessPartnersEnterAddressForm.enterAddressForm
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.BusinessPartnersEnterAddress
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject

class BusinessPartnersUnincorporatedBodyEnterAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  val partnerName = "Test Unincorporated Body"
  val bpAddressForm: Form[BusinessPartnersEnterAddress] = enterAddressForm
  val postAction: Call = routes.BusinessPartnersUnincorporatedBodyEnterAddressController.next()
  val backLink: String =
    routes.BusinessPartnersUnincorporatedBodyRegisteredAddressController.load().url

  import actions._

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      // Todo get this from cache later
      Ok(
        view
          .business_partners_enter_registered_address(enterAddressForm, postAction, partnerName, backLink))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      // Todo get this from cache later
      enterAddressForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(
              view.business_partners_enter_registered_address(formWithErrors, postAction, partnerName, backLink))
          },
          bpAddress => {
            Redirect(routes.BusinessPartnersCheckYourAnswersController.load())
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

}
