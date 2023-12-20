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
import models.{Mode, NormalMode}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.fhregistrationfrontend.views.Views

@Singleton
class BusinessPartnersConfirmAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {
  import actions._

  val address: Address = Address(
    addressLine1 = "1 Romford Road",
    addressLine2 = Some("Wellington"),
    addressLine3 = Some("Telford"),
    addressLine4 = None,
    postcode = "TF1 4ER",
    countryCode = None,
    lookupId = None
  )

  val partnerName = "test business partner"
  val backLink = routes.BusinessPartnerAddressController.load().url

  def load(index: Int, mode: Mode = NormalMode): Action[AnyContent] = dataRequiredAction { implicit request =>
    val postAction: Call = routes.BusinessPartnersConfirmAddressController.next(index, mode)
    Ok(view.business_partners_confirm_partner_address(address, partnerName, postAction, backLink))
  }

  def next(index: Int, mode: Mode = NormalMode): Action[AnyContent] = dataRequiredAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Redirect(routes.BusinessPartnersCheckYourAnswersController.load())
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
}
