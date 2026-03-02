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
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.controllers.routes
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.des
import models.UserAnswers
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterKeys.{businessRegistrationDetailsKey, displayDesDeclarationKey, displayKeyForPage}
import uk.gov.hmrc.fhregistrationfrontend.services.{Save4LaterKeys, Save4LaterService}

import scala.concurrent.{ExecutionContext, Future}

class JourneyRequest[A](
  userAnswers: UserAnswers,
  request: UserRequest[A],
  val bpr: BusinessRegistrationDetails,
  val businessType: BusinessType,
  val verifiedEmail: String,
  val journeyType: JourneyType,
  val journeyPages: JourneyPages,
  val journeyState: JourneyState
) extends WrappedRequest[A](request) {
  def userId: String = request.userId

  def registrationNumber = request.registrationNumber

  def lastUpdateTimestamp =
    userAnswers.getEntry[Long](Save4LaterKeys.userLastTimeSavedKey) getOrElse 0L

  def hasUpdates: Option[Boolean] =
    journeyType match {
      case JourneyType.Amendment | JourneyType.Variation => Some(pagesHaveAmendments || verifiedEmailHasAmendments)
      case _                                             => None
    }

  private def pagesHaveAmendments = journeyPages.pages exists (page => pageHasAmendments(page))
  private def verifiedEmailHasAmendments = displayVerifiedEmail exists (_ != verifiedEmail)

  private def pageHasAmendments[T](page: Page[T]) =
    userAnswers.getEntry[T](page.id)(using page.format) != userAnswers.getEntry[T](displayKeyForPage(page.id))(
      using page.format
    )

  def displayPageDataLoader = new PageDataLoader {
    override def pageDataOpt[T](page: Page[T]): Option[T] =
      userAnswers.getEntry[T](displayKeyForPage(page.id))(using page.format)
  }

  def displayDeclaration =
    userAnswers.getEntry[des.Declaration](displayDesDeclarationKey)

  def displayVerifiedEmail =
    userAnswers.getEntry[String](Save4LaterKeys.displayKeyForPage(Save4LaterKeys.verifiedEmailKey))

}

class JourneyAction @Inject() (journeys: Journeys)(implicit
  val save4LaterService: Save4LaterService,
  errorHandler: ErrorHandler,
  val executionContext: ExecutionContext
) extends ActionRefiner[UserRequest, JourneyRequest] with FrontendAction with ActionFunctions {

  override def refine[A](input: UserRequest[A]): Future[Either[Result, JourneyRequest[A]]] = {
    implicit val r: UserRequest[A] = input

    val result: EitherT[Future, Result, JourneyRequest[A]] = for {
      userAnswers   <- EitherT.liftF(loadUserAnswers)
      bpr           <- findBpr(userAnswers).toEitherT[Future]
      bt            <- getBusinessType(userAnswers).toEitherT[Future]
      verifiedEmail <- findVerifiedEmail(userAnswers).toEitherT[Future]
      journeyType = loadJourneyType(userAnswers)
      journeyPages <- getJourneyPages(userAnswers).toEitherT[Future]
      journeyState = loadJourneyState(journeyPages)
    } yield new JourneyRequest[A](userAnswers, r, bpr, bt, verifiedEmail, journeyType, journeyPages, journeyState)

    result.value
  }

  def findVerifiedEmail(userAnswers: UserAnswers): Either[Result, String] =
    userAnswers
      .getEntry[String](Save4LaterKeys.verifiedEmailKey)
      .fold(
        Either.left[Result, String](Redirect(routes.EmailVerificationController.emailVerificationStatus))
      )(verifiedEmail => Either.right(verifiedEmail))

  def findBpr(userAnswers: UserAnswers)(implicit request: Request[?]): Either[Result, BusinessRegistrationDetails] =
    userAnswers.getEntry[BusinessRegistrationDetails](businessRegistrationDetailsKey) match {
      case Some(bpr) => Right(bpr)
      case None =>
        logger.error(s"Not found bpr")
        Left(errorHandler.errorResultsPages(Results.BadRequest))

    }

  def getBusinessType(userAnswers: UserAnswers)(implicit request: Request[?]): Either[Result, BusinessType] =
    userAnswers.getEntry[BusinessType](Save4LaterKeys.businessTypeKey) match {
      case Some(bt) => Right(bt)
      case None =>
        logger.error(s"Not found business type")
        Left(errorHandler.errorResultsPages(Results.BadRequest))

    }

  def getJourneyPages(userAnswers: UserAnswers)(implicit request: Request[?]): Either[Result, JourneyPages] = {
    val pagesForEntityType = getBusinessType(userAnswers).flatMap {
      _ match {
        case BusinessType.CorporateBody => Right(journeys.limitedCompanyPages)
        case BusinessType.SoleTrader    => Right(journeys.soleTraderPages)
        case BusinessType.Partnership   => Right(journeys.partnershipPages)
        case _ =>
          logger.error(s"Not found: wrong business type")
          Left(errorHandler.errorResultsPages(Results.BadRequest))

      }
    }

    pagesForEntityType map { pages =>
      val pagesWithData = pages map { page =>
        pageWithData(userAnswers)(page)
      }
      new JourneyPages(pagesWithData)
    }

  }

  private def pageWithData[T](userAnswers: UserAnswers)(page: Page[T]) =
    userAnswers.getEntry(page.id)(using page.format) match {
      case Some(data) => page `withData` data
      case None       => page
    }

  def loadJourneyState(journeyPages: JourneyPages): JourneyState =
    journeys.journeyState(journeyPages)
}
