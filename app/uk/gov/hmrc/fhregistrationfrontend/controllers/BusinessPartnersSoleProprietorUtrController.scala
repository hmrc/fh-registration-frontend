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
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersEnterUtrForm.businessPartnersEnterUtrForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views

@Singleton
class BusinessPartnersSoleProprietorUtrController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {
  import actions._

  val partnerName: String = "{{partner name}}"
  val postAction: Call = routes.BusinessPartnersSoleProprietorUtrController.next()
  val businessPartnerType: String = "SoleProprietor"
  val backLink: String = routes.BusinessPartnersVatRegistrationNumberController.load().url

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(
        view.business_partners_enter_utr_number(
          businessPartnersEnterUtrForm,
          partnerName,
          businessPartnerType,
          postAction,
          backLink))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      businessPartnersEnterUtrForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(
              view.business_partners_enter_utr_number(
                formWithErrors,
                partnerName,
                businessPartnerType,
                postAction,
                backLink))
          },
          businessPartnersUtr => Redirect(routes.BusinessPartnersAddressController.load())
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
}
