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
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessCustomersForm.businessCustomersForm
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation.noNavigation

import javax.inject.Inject

class BusinessCustomersController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._

  val postAction = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.BusinessCustomersController.next()

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(view.business_customers(businessCustomersForm, noNavigation, postAction))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      businessCustomersForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(view.business_customers(formWithErrors, noNavigation, postAction))
        },
        numberOfCustomers => {
          Ok(s"Next page! with form result: $numberOfCustomers")
        }
      )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

}
