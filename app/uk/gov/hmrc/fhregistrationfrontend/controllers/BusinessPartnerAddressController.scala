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

import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.fhregistrationfrontend.actions.{Actions, PageRequest}
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersAddressForm.businessPartnersAddressForm
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Rendering
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject

class BusinessPartnerAddressController @Inject()(ds: CommonPlayDependencies, view: Views, actions: Actions)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._
  def load(): Action[AnyContent] = userAction { implicit request =>
    val bpAddressForm = businessPartnersAddressForm
    val postAction =
      Call(
        method = "POST",
        url = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.BusinessPartnerAddressController.load().url)
    Ok(view.business_partners_address(bpAddressForm, postAction))
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    businessPartnersAddressForm.bindFromRequest.fold(
      formWithErrors => {
        val postAction =
          Call(
            method = "POST",
            url = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.BusinessPartnerAddressController.next().url)
        BadRequest(view.business_partners_address(formWithErrors, postAction))
      },
      bpAddress => {
        Ok(s"Next page! with form result: ${bpAddress.toString}")
      }
    )
  }

}
