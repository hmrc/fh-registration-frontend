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
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, BusinessPartnerIndividual, BusinessPartnerLimitedLiabilityPartnership}
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.views.businessPartners.v2.summary.{IndividualSummaryHelper, LLPSummaryHelper}
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow

@Singleton
class BusinessPartnersCheckYourAnswersController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._

  val businessPartnerType = "individual"

  val address: Address = Address(
    addressLine1 = "1 Romford Road",
    addressLine2 = Some("Wellington"),
    addressLine3 = Some("Telford"),
    addressLine4 = None,
    postcode = "TF1 4ER",
    countryCode = None,
    lookupId = None
  )

  val individualSummaryModel =
    BusinessPartnerIndividual("first name", "last name", hasNino = true, Some("QQ123456C"), address)

  val llpSummaryModel = BusinessPartnerLimitedLiabilityPartnership(
    "llp trading name",
    hasTradeName = true,
    Some("trade name"),
    "01234567",
    hasVat = true,
    vat = Some("123456789"),
    uniqueTaxpayerReference = Some("1234567890"),
    address
  )

  // TODO temp solution for it testing - passes data cleaner than using cookies as workaround
  // TODO remove when cache is implemented
  def load(partnerType: String): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      val summaryRows = getRowsBasedOnPartnerType(partnerType)
      if (summaryRows.nonEmpty) {
        Ok(view.business_partners_check_your_answers("#", summaryRows))
      } else {
        logger.warn("[BusinessPartnersCheckYourAnswersController][load] - Unable to create CYA summary rows")
        errorHandler.errorResultsPages(Results.BadRequest)
      }
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      Ok(s"Form submitted, with result:")
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  private def getRowsBasedOnPartnerType(partnerType: String)(implicit messages: Messages): Seq[SummaryListRow] = {

    val partnerTypeWithModel = Map(
      "individual"                    -> individualSummaryModel,
      "limited-liability-partnership" -> llpSummaryModel
    )

    partnerTypeWithModel(partnerType) match {
      case individual: BusinessPartnerIndividual           => IndividualSummaryHelper(individual)
      case llp: BusinessPartnerLimitedLiabilityPartnership => LLPSummaryHelper(llp)
      case _                                               => Seq.empty
    }

  }
}
