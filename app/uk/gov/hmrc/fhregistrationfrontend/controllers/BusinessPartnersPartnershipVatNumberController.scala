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

@Singleton
class BusinessPartnersPartnershipVatNumberController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {
  import actions._

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      val form = vatNumberForm
      //ToDo read this data from the cache after being stored before the redirect
      val partnerName = "test partner"
      Ok(view.business_partner_partnership_vat_number(form, partnerName))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      //ToDo read this data from the cache after being stored before the redirect
      val partnerName = "test partner"
      vatNumberForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(view.business_partner_partnership_vat_number(formWithErrors, partnerName))
        },
        vatNumber => {
          vatNumber.value match {
            case Some(vatNumber) => Ok(s"Next page! with vatNumber: $vatNumber")
            case None =>
              Ok(s"Next page! with no vatNumber")
          }
        }
      )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
}