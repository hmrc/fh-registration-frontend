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
import models.NormalMode
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.views.Views

@Singleton
class BusinessPartnersCannotFindAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {
  import actions._

  val partnerName = "Test User"

  def load(): Action[AnyContent] = userAction { implicit request =>
    val backLinkAndButtonUrl: String = getUrlFromBusinessType(
      routes.BusinessPartnerPartnershipRegisteredAddressController.load().url,
      routes.BusinessPartnerAddressController.load().url,
      routes.BusinessPartnersCorporateBodyRegisteredAddressController.load().url,
      routes.BusinessPartnersUnincorporatedBodyRegisteredAddressController.load().url,
      config.getRandomBusinessType()
    )

    val manuallyEnterAddressUrl: String = getUrlFromBusinessType(
      routes.BusinessPartnersEnterRegistrationOfficeAddress.load().url,
      routes.BusinessPartnersEnterAddressController.load(1, NormalMode).url,
      routes.BusinessPartnersCorporateBodyEnterAddressController.load().url,
      routes.BusinessPartnersUnincorporatedOfficeAddressController.load().url,
      config.getRandomBusinessType()
    )

    if (config.newBusinessPartnerPagesEnabled) {
      Ok(
        view.business_partners_cannot_find_address(
          "HR33 7GP",
          partnerName,
          backAction = backLinkAndButtonUrl,
          manuallyEnterAddressUrl = manuallyEnterAddressUrl,
          buttonUrl = backLinkAndButtonUrl
        )
      )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def getUrlFromBusinessType(url1: String, url2: String, url3: String, url4: String, partnerType: String): String =
    if (partnerType == "partnership" || partnerType == "limited-liability-partnership") url1
    else if (partnerType == "individual" || partnerType == "sole-proprietor") url2
    else if (partnerType == "corporateBody") url3
    else if (partnerType == "unincorporated-body") url4
    else "#"
}
