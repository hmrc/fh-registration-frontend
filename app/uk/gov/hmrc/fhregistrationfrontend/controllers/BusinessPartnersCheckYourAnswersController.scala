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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessPartnerIndividual, BusinessPartnerLimitedLiabilityPartnership, BusinessPartnerUnincorporatedBody}
import uk.gov.hmrc.fhregistrationfrontend.utils.TestData
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.views.businessPartners.v2.summary._
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
      "individual"                    -> TestData.individualSummaryModel,
      "limited-liability-partnership" -> TestData.llpSummaryModel,
      "limited-liability-partnership-with-vat-and-trading-name" -> TestData.llpSummaryModel
        .copy(hasTradeName = true, hasVat = true),
      "sole-proprietor"          -> soleProprietorSummaryModel,
      "sole-proprietor-with-vat" -> soleProprietorSummaryModel.copy(hasVat = true),
      "partnership"              -> partnershipModel,
      "partnership-with-optional-values" -> partnershipModel
        .copy(hasTradeName = true, hasVat = true, hasUniqueTaxpayerReference = true)
        .copy(hasTradeName = true, hasVat = true),
      "unincorporated-body" -> TestData.unincoporateBodyModel,
      "unincorporated-body-optional-values" -> TestData.unincoporateBodyModel
        .copy(hasTradeName = true, hasVat = true, hasUniqueTaxpayerReference = true)
    )

    partnerTypeWithModel(partnerType) match {
      case individual: BusinessPartnerIndividual           => IndividualSummaryHelper(individual)
      case llp: BusinessPartnerLimitedLiabilityPartnership => LLPSummaryHelper(llp)
      case soleProprietor: BusinessPartnerSoleProprietor   => SoleProprietorSummaryHelper(soleProprietor)
      case partnership: BusinessPartnerPartnership         => PartnershipSummaryHelper(partnership)
      case uBody: BusinessPartnerUnincorporatedBody        => UnincorporatedBodySummaryHelper(uBody)
      case _                                               => Seq.empty
    }

  }
}
