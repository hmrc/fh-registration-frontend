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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.forms.BusinessPartnersV2.BusinessPartnerNino.businessPartnerNinoForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RadioHelper

import javax.inject.Inject

class TestController @Inject()(radioHelper: RadioHelper, ds: CommonPlayDependencies, view: Views, actions: Actions)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._
  def load(): Action[AnyContent] = userAction { implicit request =>
    val ninoForm = businessPartnerNinoForm
    val items = radioHelper.conditionalYesNoRadio(ninoForm)
    Ok(view.business_partners_v2(ninoForm, items))
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    businessPartnerNinoForm.bindFromRequest.fold(
      formWithErrors => {
        val items = radioHelper.conditionalYesNoRadio(formWithErrors)
        BadRequest(view.business_partners_v2(formWithErrors, items))
      },
      nino => {
        Ok(s"Next page! with form result: ${nino.toString}")
      }
    )
  }

}
