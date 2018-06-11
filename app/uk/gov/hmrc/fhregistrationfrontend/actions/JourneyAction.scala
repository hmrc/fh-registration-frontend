/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.controllers.routes
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterKeys.businessRegistrationDetailsKey
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterKeys.displayKeyForPage
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterKeys.displayDesDeclarationKey
import uk.gov.hmrc.fhregistrationfrontend.services.{Save4LaterKeys, Save4LaterService}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.fhregistrationfrontend.models.des

import scala.concurrent.Future

class JourneyRequest[A](
  cacheMap: CacheMap,
  request: UserRequest[A],
  val bpr: BusinessRegistrationDetails,
  val businessType: BusinessType,
  val verifiedEmail: String,
  val journeyType: JourneyType,
  val journeyPages: JourneyPages,
  val journeyState: JourneyState
) extends WrappedRequest[A](request) with PageDataLoader
{
  def userId: String = request.userId

  def pageDataOpt[T](page: Page[T]): Option[T] =
    cacheMap.getEntry[T](page.id)(page.format)

  def registrationNumber = request.registrationNumber
//  def email: Option[String] = request.ggEmail

  def lastUpdateTimestamp = {
    cacheMap.getEntry[Long](Save4LaterKeys.userLastTimeSavedKey) getOrElse 0L
  }

  def hasUpdates: Option[Boolean] = {
    journeyType match {
      case JourneyType.Amendment | JourneyType.Variation ⇒ Some(pagesHaveAmendments || verifiedEmailHasAmendments)
      case _ ⇒ None
    }
  }

  private def pagesHaveAmendments = journeyPages.pages exists( page ⇒ pageHasAmendments(page))
  private def verifiedEmailHasAmendments = displayVerifiedEmail exists (_ != verifiedEmail)

  private def pageHasAmendments[T](page: Page[T]) = {
    cacheMap.getEntry[T](page.id)(page.format) != cacheMap.getEntry[T](displayKeyForPage(page.id))(page.format)
  }

  def displayPageDataLoader = new PageDataLoader {
    override def pageDataOpt[T](page: Page[T]): Option[T] = cacheMap.getEntry[T](displayKeyForPage(page.id))(page.format)
  }

  def displayDeclaration =
    cacheMap.getEntry[des.Declaration](displayDesDeclarationKey)

  def displayVerifiedEmail =
    cacheMap.getEntry[String](Save4LaterKeys.displayKeyForPage(Save4LaterKeys.verifiedEmailKey))

}

class JourneyAction (implicit val save4LaterService: Save4LaterService, errorHandler: ErrorHandler)
  extends ActionRefiner[UserRequest, JourneyRequest]
    with FrontendAction
    with ActionFunctions
{

  override def refine[A](input: UserRequest[A]): Future[Either[Result, JourneyRequest[A]]] = {
    implicit val r: UserRequest[A] = input

    val result: EitherT[Future, Result, JourneyRequest[A]] = for {
      cacheMap ← EitherT(loadCacheMap)
      bpr ← findBpr(cacheMap).toEitherT[Future]
      bt ← getBusinessType(cacheMap).toEitherT[Future]
      verifiedEmail ← findVerifiedEmail(cacheMap).toEitherT[Future]
      journeyType = loadJourneyType(cacheMap)
      journeyPages ← getJourneyPages(cacheMap).toEitherT[Future]
      journeyState = loadJourneyState(journeyPages)
    } yield {
      new JourneyRequest[A](
        cacheMap,
        r,
        bpr,
        bt,
        verifiedEmail,
        journeyType,
        journeyPages,
        journeyState)
    }

    result.value
  }


  def findVerifiedEmail(cacheMap: CacheMap)(implicit request: UserRequest[_]): Either[Result, String] = {
    cacheMap.getEntry[String](Save4LaterKeys.verifiedEmailKey).fold(
      Either.left[Result, String](Redirect(routes.EmailVerificationController.emailVerificationStatus()))
    )(
      verifiedEmail ⇒ Either.right(verifiedEmail)
    )
  }

  def findBpr(cacheMap: CacheMap)(implicit request: Request[_]): Either[Result, BusinessRegistrationDetails] = {
    cacheMap.getEntry[BusinessRegistrationDetails](businessRegistrationDetailsKey) match {
      case Some(bpr) ⇒ Right(bpr)
      case None ⇒
        Logger.error(s"Not found bpr")
        Left(errorHandler.errorResultsPages(Results.BadRequest))

    }
  }

  def getBusinessType(cacheMap: CacheMap)(implicit request: Request[_]): Either[Result, BusinessType] = {
    cacheMap.getEntry[BusinessType](Save4LaterKeys.businessTypeKey) match {
      case Some(bt) ⇒ Right(bt)
      case None ⇒
        Logger.error(s"Not found business type")
        Left(errorHandler.errorResultsPages(Results.BadRequest))

    }
  }

  def getJourneyPages(cacheMap: CacheMap)(implicit request: Request[_]): Either[Result, JourneyPages] = {
    val pagesForEntityType = getBusinessType(cacheMap).right flatMap {
      _ match {
        case BusinessType.CorporateBody ⇒ Right(Journeys.limitedCompanyPages)
        case BusinessType.SoleTrader    ⇒ Right(Journeys.soleTraderPages)
        case BusinessType.Partnership   ⇒ Right(Journeys.partnershipPages)
        case _                          ⇒
          Logger.error(s"Not found: wrong business type")
          Left(errorHandler.errorResultsPages(Results.BadRequest))

      }
    }

    pagesForEntityType map {pages ⇒
      val pagesWithData = pages map { page ⇒ pageWithData(cacheMap)(page)}
      new JourneyPages(pagesWithData)
    }

  }

  private def pageWithData[T](cacheMap: CacheMap)(page: Page[T]) = {
    cacheMap.getEntry(page.id)(page.format) match {
      case Some(data) ⇒ page withData data
      case None ⇒ page
    }
  }

  def loadJourneyState(journeyPages: JourneyPages): JourneyState = {
    Journeys.journeyState(journeyPages)
  }
}
