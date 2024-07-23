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

package uk.gov.hmrc.fhregistrationfrontend.actions

import cats.data.EitherT
import cats.implicits._
import com.google.inject.Inject
import play.api.mvc.{ActionRefiner, Result, WrappedRequest, _}
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page.AnyPage
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.forms.models._

import scala.concurrent.{ExecutionContext, Future}

class PageRequest[A](val journey: JourneyNavigation, p: AnyPage, request: JourneyRequest[A])
    extends WrappedRequest[A](request) {

  def page[T]: Page[T] = p.asInstanceOf[Page[T]]
  def userId: String = request.userId

  val journeyState = request.journeyState
  def lastUpdateTimestamp = request.lastUpdateTimestamp
  def bpr = request.bpr

  private def pageDataOpt[T](pageId: String): Option[T] =
    request.journeyPages.get(pageId).flatMap((p: Page[T]) => p.data)

  private def vatReg(): Option[VatNumber] = pageDataOpt("vatNumber")

  def companyOfficers(): ListWithTrackedChanges[CompanyOfficer] =
    pageDataOpt("companyOfficers").getOrElse(ListWithTrackedChanges.empty[CompanyOfficer]())

  def businessPartners(): ListWithTrackedChanges[BusinessPartner] =
    pageDataOpt("businessPartners").getOrElse(ListWithTrackedChanges.empty[BusinessPartner]())

  def otherUsedVatNumbers(vatNumberPageData: VatNumber): List[String] = {
    val usedCompanyOfficers: List[CompanyOfficer] = companyOfficers().values.toList
    val usedBusinessPartners: List[BusinessPartner] = businessPartners().values.toList
    val usedVatRegInCompanyOfficers: List[String] = usedCompanyOfficers
      .map(_.identification)
      .flatMap(_ match {
        case co: CompanyOfficerCompany   => co.vat
        case _: CompanyOfficerIndividual => None
      })
    val usedVatRegInBusinessPartners: List[String] = usedBusinessPartners
      .map(_.identification)
      .flatMap(_ match {
        case s: BusinessPartnerSoleProprietor              => s.vat
        case p: BusinessPartnerPartnership                 => p.vat
        case l: BusinessPartnerLimitedLiabilityPartnership => l.vat
        case c: BusinessPartnerCorporateBody               => c.vat
        case u: BusinessPartnerUnincorporatedBody          => u.vat
        case _: BusinessPartnerIndividual                  => None
      })
    usedVatRegInCompanyOfficers ++ usedVatRegInBusinessPartners
  }

  def otherUsedVatNumbers(businessPartnersPageData: List[BusinessPartner], sectionId: Option[String]): List[String] = {
//    TODO: IMPLEMENT THIS METHOD
    val usedCompanyOfficers: List[CompanyOfficer] = companyOfficers().values.toList
    val usedBusinessPartners: List[BusinessPartner] = businessPartners().values.toList
    val usedVatRegInCompanyOfficers: List[String] = usedCompanyOfficers
      .map(_.identification)
      .flatMap(_ match {
        case co: CompanyOfficerCompany   => co.vat
        case _: CompanyOfficerIndividual => None
      })
    val usedVatRegInBusinessPartners: List[String] = usedBusinessPartners
      .map(_.identification)
      .flatMap(_ match {
        case s: BusinessPartnerSoleProprietor              => s.vat
        case p: BusinessPartnerPartnership                 => p.vat
        case l: BusinessPartnerLimitedLiabilityPartnership => l.vat
        case c: BusinessPartnerCorporateBody               => c.vat
        case u: BusinessPartnerUnincorporatedBody          => u.vat
        case _: BusinessPartnerIndividual                  => None
      })
    usedVatRegInCompanyOfficers ++ usedVatRegInBusinessPartners
  }
}

//TODO all exceptional results need to be reviewed
class PageAction[T, V] @Inject() (pageId: String, sectionId: Option[String], journey: Journeys)(implicit
  errorHandler: ErrorHandler,
  val executionContext: ExecutionContext
) extends ActionRefiner[JourneyRequest, PageRequest] with FrontendAction {

  override def refine[A](input: JourneyRequest[A]): Future[Either[Result, PageRequest[A]]] = {
    implicit val r: JourneyRequest[A] = input

    val result: EitherT[Future, Result, PageRequest[A]] = for {

      pageWithData    <- loadPage(input).toEitherT[Future]
      _               <- accessiblePage(pageWithData, input.journeyState).toEitherT[Future]
      pageWithSection <- loadPageSection(pageWithData).toEitherT[Future]
      journeyNavigation = loadJourneyNavigation(input.journeyPages, input.journeyState)
    } yield new PageRequest(
      journeyNavigation,
      pageWithSection,
      input
    )
    result.value
  }

  def accessiblePage(page: AnyPage, state: JourneyState)(implicit request: Request[_]): Either[Result, Boolean] =
    if (state.isPageComplete(page) || state.nextPageToComplete().contains(page.id)) {
      Right(true)
    } else {
      Left(errorHandler.errorResultsPages(Results.NotFound))
    }

  def loadPageSection(page: Page[T])(implicit request: Request[_]): Either[Result, Page[T]] =
    if (page.withSubsection isDefinedAt sectionId)
      Right(page withSubsection sectionId)
    else {
      Left(errorHandler.errorResultsPages(Results.NotFound))
    }

  def loadPage[A](request: JourneyRequest[A]): Either[Result, Page[T]] =
    request.journeyState.get[T](pageId) match {
      case Some(page) => Right(page)
      case None =>
        Left(errorHandler.errorResultsPages(Results.NotFound)(request))
    }

  def loadJourneyNavigation(journeyPages: JourneyPages, state: JourneyState) =
    if (state.isComplete)
      journey.summaryJourney(journeyPages)
    else
      journey.linearJourney(journeyPages)
}
