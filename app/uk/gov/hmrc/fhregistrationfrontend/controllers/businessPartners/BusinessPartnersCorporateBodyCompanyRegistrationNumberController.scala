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

import com.google.inject.{Inject, Singleton}
import models.NormalMode
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.controllers.{AppController, CommonPlayDependencies}
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.CompanyRegistrationNumberForm._
import uk.gov.hmrc.fhregistrationfrontend.views.Views

@Singleton
class BusinessPartnersCorporateBodyCompanyRegistrationNumberController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {
  import actions._

  val businessType: String = "corporateBody"
  val companyName = "Test CorporateBody"
  val backLink = routes.BusinessPartnersCorporateBodyTradingNameController.load(1, NormalMode).url
  val postAction = routes.BusinessPartnersCorporateBodyCompanyRegistrationNumberController.next()

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      //ToDo read this data from the cache after being stored before the redirect
      Ok(
        view
          .business_partners_enter_crn(companyRegistrationNumberForm, companyName, businessType, postAction, backLink))
        .withCookies(Cookie("businessType", businessType))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      //ToDo read this data from the cache after being stored before the redirect
      companyRegistrationNumberForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(
              view.business_partners_enter_crn(formWithErrors, companyName, businessType, postAction, backLink)
            )
          },
          regNumber => {
            regNumber.crnFormatted match {
              case Some(_) =>
                Redirect(routes.BusinessPartnersCorporateBodyVatNumberController.load())
              case _ => Redirect(routes.PartnerTypeController.load(0, NormalMode))
            }
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
}
