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
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.ContactPersonForm.contactPersonForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address

import javax.inject.Inject

class ContactPersonController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._

  val address: Address = Address(
    addressLine1 = "23 High Street",
    addressLine2 = Some("Park View"),
    addressLine3 = Some("Gloucester"),
    addressLine4 = Some("Gloucestershire"),
    postcode = "NE98 1ZZ",
    countryCode = None,
    lookupId = None
  )

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(view.contact_person_page(contactPersonForm, address))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      contactPersonForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(view.contact_person_page(formWithErrors, address))
          },
          contactPerson => {
            Ok(s"Form submitted, with result: $contactPerson")
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

}
