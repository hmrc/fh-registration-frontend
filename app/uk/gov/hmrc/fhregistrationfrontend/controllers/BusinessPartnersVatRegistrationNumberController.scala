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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.VatNumberForm.vatNumberForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import models.NormalMode

@Singleton
class BusinessPartnersVatRegistrationNumberController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {
  import actions._

  //ToDo read this data from the cache after being stored before the redirect
  val partnerName = "test partner"
  val backUrl: String = routes.BusinessPartnersIndividualsAndSoleProprietorsNinoController.load(1, NormalMode).url

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(view.business_partners_enter_vat_registration(vatNumberForm, partnerName, backUrl))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      vatNumberForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(view.business_partners_enter_vat_registration(formWithErrors, partnerName, backUrl))
          },
          vatNumber => {
            vatNumber.value match {
              case Some(vatNumber) => Redirect(routes.BusinessPartnersAddressController.load())
              case None            => Redirect(routes.BusinessPartnersSoleProprietorUtrController.load())
            }
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
}
