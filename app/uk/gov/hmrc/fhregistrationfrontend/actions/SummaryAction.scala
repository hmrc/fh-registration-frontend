/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{JourneyState, Page, PageDataLoader}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

import scala.concurrent.Future

class SummaryRequest[A](
  val journeyRequest: JourneyRequest[A]
) extends WrappedRequest[A](journeyRequest) with PageDataLoader
{
  def userId: String = journeyRequest.userId

  def registrationNumber = journeyRequest.registrationNumber

  val bpr = journeyRequest.bpr
  val businessType = journeyRequest.businessType
  val verifiedEmail = journeyRequest.verifiedEmail

  def pageDataOpt[T](page: Page[T]): Option[T] = journeyRequest.journeyPages.get(page.id).flatMap((p: Page[T]) ⇒ p.data)

}


class SummaryAction(implicit errorHandler: ErrorHandler)
  extends ActionRefiner[JourneyRequest, SummaryRequest] with FrontendAction {

  override protected def refine[A](input: JourneyRequest[A]): Future[Either[Result, SummaryRequest[A]]] = {
    implicit val r: JourneyRequest[A] = input
    val result: EitherT[Future, Result, SummaryRequest[A]] = for {
      _ ← journeyIsComplete(input.journeyState).toEitherT[Future]
    } yield {
      new SummaryRequest[A](input)
    }

    result.value
  }

  def journeyIsComplete(journeyState: JourneyState)(implicit request: Request[_]): Either[Result, Boolean] = {
    if(journeyState.isComplete)
      Right(true)
    else {
      Logger.error(s"Bad Request")
      Left(errorHandler.errorResultsPages(Results.BadRequest))
    }
  }

}