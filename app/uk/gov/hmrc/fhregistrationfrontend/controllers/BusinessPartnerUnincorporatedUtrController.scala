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
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersUtrForm.businessPartnerUtrForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views

@Singleton
class BusinessPartnerUnincorporatedUtrController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {
  import actions._

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      val form = businessPartnerUtrForm
      //Todo get partnerName from cache
      val partnerName = "{{Unincorporated body name}}"
      val postAction =
        Call(
          method = "POST",
          url =
            uk.gov.hmrc.fhregistrationfrontend.controllers.routes.BusinessPartnerUnincorporatedUtrController.load().url)
      Ok(view.business_partners_utr(form, postAction, partnerName))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      //Todo get partnerName from cache
      val partnerName = "{{Unincorporated body name}}"
      businessPartnerUtrForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val postAction =
              Call(
                method = "POST",
                url = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.BusinessPartnerUnincorporatedUtrController
                  .next()
                  .url)
            BadRequest(view.business_partners_utr(formWithErrors, postAction, partnerName))
          },
          businessPartnersUtr => {
            businessPartnersUtr.value match {
              case Some(businessPartnersUtr) => Ok(s"Next page! with UTR: $businessPartnersUtr")
              case None =>
                Ok(s"Next page! with no UTR")
            }
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
}
