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
import play.api.i18n.MessagesApi
import play.api.mvc._
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
  val journeyPages: JourneyPages,
  val journeyState: JourneyState
) extends WrappedRequest[A](request) with PageDataLoader
{
  def userId: String = request.userId

  def pageDataOpt[T](page: Page[T]): Option[T] =
    cacheMap.getEntry[T](page.id)(page.format)

  def registrationNumber = request.registrationNumber
  def userIsRegistered = request.userIsRegistered
  def email: Option[String] = request.email

  def lastUpdateTimestamp = {
    cacheMap.getEntry[Long](Save4LaterKeys.userLastTimeSavedKey) getOrElse 0L
  }

  def isAmendmentJourney =
    request.userIsRegistered && cacheMap.getEntry[Boolean](Save4LaterKeys.isAmendmentKey).getOrElse(false)

  def hasAmendments: Option[Boolean] = {
    if (isAmendmentJourney) Some(journeyPages.pages exists( page ⇒ pageHasAmendments(page)))
    else None
  }

  private def pageHasAmendments[T](page: Page[T]) = {
    cacheMap.getEntry[T](page.id)(page.format) != cacheMap.getEntry[T](displayKeyForPage(page.id))(page.format)
  }

  def displayPageDataLoader = new PageDataLoader {
    override def pageDataOpt[T](page: Page[T]): Option[T] = cacheMap.getEntry[T](displayKeyForPage(page.id))(page.format)
  }

  def displayDeclaration =
    cacheMap.getEntry[des.Declaration](displayDesDeclarationKey)
}

object JourneyAction {
  def apply()(implicit save4LaterService: Save4LaterService, messagesApi: MessagesApi) =
    new UserAction() andThen new NoEnrolmentCheckAction andThen new JourneyAction
}

class JourneyAction (implicit val save4LaterService: Save4LaterService, val messagesApi: MessagesApi)
  extends ActionRefiner[UserRequest, JourneyRequest]
    with FrontendAction
{

  override def refine[A](input: UserRequest[A]): Future[Either[Result, JourneyRequest[A]]] = {
    implicit val r: UserRequest[A] = input

    val result: EitherT[Future, Result, JourneyRequest[A]] = for {
      cacheMap ← EitherT(loadCacheMap)
      _ ← checkAmendmentJourney(cacheMap).toEitherT[Future]
      journeyPages ← getJourneyPages(cacheMap).toEitherT[Future]
      journeyState = loadJourneyState(journeyPages, cacheMap)
      bpr ← findBpr(cacheMap).toEitherT[Future]
      bt ← getBusinessType(cacheMap).toEitherT[Future]
    } yield {
      new JourneyRequest[A](
        cacheMap,
        r,
        bpr,
        bt,
        journeyPages,
        journeyState)
    }

    result.value
  }

  def checkAmendmentJourney(cacheMap: CacheMap)(implicit request: UserRequest[_]): Either[Result, Boolean] = {
    if (request.userIsRegistered)
      if (cacheMap.getEntry[Boolean](Save4LaterKeys.isAmendmentKey).isDefined)
        Right(true)
      else {
        Logger.error(s"Journey request with enrolment but no amendment in progress")
        Left(errorResultsPages(Results.BadRequest))
      }
    else
      Right(true)
  }

  def loadCacheMap(implicit save4LaterService: Save4LaterService, request: UserRequest[_]): Future[Either[Result, CacheMap]] = {
    save4LaterService.shortLivedCache.fetch(request.userId) map {
      case Some(cacheMap) ⇒ Right(cacheMap)
      case None ⇒
        Logger.error(s"Not found in shortLivedCache")
        Left(errorResultsPages(Results.NotFound))
    } recover { case t ⇒
      Logger.error(s"Could not access shortLivedCache", t)
      Left(errorResultsPages(Results.BadGateway))
    }
  }

  def findBpr(cacheMap: CacheMap)(implicit request: Request[_]): Either[Result, BusinessRegistrationDetails] = {
    cacheMap.getEntry[BusinessRegistrationDetails](businessRegistrationDetailsKey) match {
      case Some(bpr) ⇒ Right(bpr)
      case None ⇒
        Logger.error(s"Not found bpr")
        Left(errorResultsPages(Results.NotFound))
    }
  }

  def getBusinessType(cacheMap: CacheMap)(implicit request: Request[_]): Either[Result, BusinessType] = {
    cacheMap.getEntry[BusinessType](Save4LaterKeys.businessTypeKey) match {
      case Some(bt) ⇒ Right(bt)
      case None ⇒
        Logger.error(s"Not found business type")
        Left(errorResultsPages(Results.NotFound))
    }
  }

  def getJourneyPages(cacheMap: CacheMap)(implicit request: Request[_]): Either[Result, JourneyPages] = {
    getBusinessType(cacheMap).right flatMap {
      _ match {
        case BusinessType.CorporateBody ⇒ Right(Journeys.limitedCompanyPages)
        case BusinessType.SoleTrader    ⇒ Right(Journeys.soleTraderPages)
        case BusinessType.Partnership   ⇒ Right(Journeys.partnershipPages)
        case _                          ⇒
          Logger.error(s"Not found: wrong business type")
          Left(errorResultsPages(Results.NotFound))
      }
    }
  }

  def loadJourneyState(journeyPages: JourneyPages, cacheMap: CacheMap): JourneyState = {
    Journeys.journeyState(journeyPages, cacheMap)
  }
}
