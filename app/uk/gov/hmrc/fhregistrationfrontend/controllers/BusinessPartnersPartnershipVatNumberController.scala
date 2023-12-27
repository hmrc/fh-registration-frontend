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
import play.api.data.Form
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.VatNumberForm.vatNumberForm
import uk.gov.hmrc.fhregistrationfrontend.forms.models.VatNumber
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

  val form: Form[VatNumber] = vatNumberForm
  val partnerName = "Test Partner"
  val businessPartnerType = "partnership"
  val postUrl = routes.BusinessPartnersPartnershipVatNumberController.next()

  def getBusinessType: String = config.getRandomBusinessType

  val backUrl: String = {
    if (getBusinessType == "partnership")
      routes.BusinessPartnerPartnershipTradingNameController.load().url
    else if (getBusinessType == "limited-liability-partnership")
      routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load().url
    else
      "#"
  }

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      //ToDo read this data from the cache after being stored before the redirect
      Ok(view.business_partners_has_vat_number(form, businessPartnerType, partnerName, postUrl, backUrl))
        .withCookies(Cookie("businessType", getBusinessType))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      //ToDo read this data from the cache after being stored before the redirect
      vatNumberForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(
              view.business_partners_has_vat_number(formWithErrors, businessPartnerType, partnerName, postUrl, backUrl))
          },
          vatNumber => {
            request.cookies.get("businessType").map(_.value) match {
              case Some(businessType)
                  if businessType.equals("partnership") || (businessType
                    .equals("limited-liability-partnership") && vatNumber.value.isEmpty) =>
                Redirect(routes.BusinessPartnersPartnershipUtrController.load())
              case Some(businessType) if businessType.equals("limited-liability-partnership") && vatNumber.hasValue =>
                Redirect(routes.BusinessPartnersPartnershipRegisteredAddressController.load())
              case Some(unknownBusinessType) =>
                logger.warn(
                  s"[BusinessPartnersPartnershipVatNumberController][next]: Unexpected error, $unknownBusinessType retrieved")
                errorHandler.errorResultsPages(Results.BadRequest)
              case _ =>
                logger.error(
                  s"[BusinessPartnersPartnershipVatNumberController][next]: Unknown exception, returning $INTERNAL_SERVER_ERROR")
                errorHandler.errorResultsPages(Results.InternalServerError)
            }
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
}
