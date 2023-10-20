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

import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.NationalInsuranceNumberForm.nationalInsuranceNumberForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RadioHelper

import javax.inject.Inject

class BusinessPartnerNinoController @Inject()(
  radioHelper: RadioHelper,
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._

  val postAction = routes.BusinessPartnerNinoController.next()

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      val ninoForm = nationalInsuranceNumberForm
      val items = radioHelper.conditionalYesNoRadio(ninoForm)
      Ok(view.business_partners_has_nino(ninoForm, items, postAction))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      nationalInsuranceNumberForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val items = radioHelper.conditionalYesNoRadio(formWithErrors)
            BadRequest(view.business_partners_has_nino(formWithErrors, items, postAction))
          },
          nino => {
            // Todo implement reading from legal entity page
            val ninoForIndividual = "AB123456C"
            if (nino.value.contains(ninoForIndividual)) {
              Redirect(routes.BusinessPartnerAddressController.load())
            } else {
              Redirect(routes.BusinessPartnersVatRegistrationNumberController.load())
            }
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

}
