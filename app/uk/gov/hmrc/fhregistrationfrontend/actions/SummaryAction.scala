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
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{JourneyState, Page, PageDataLoader}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

import scala.concurrent.Future

class SummaryRequest[A](
  val journeyRequest: JourneyRequest[A]
) extends WrappedRequest[A](journeyRequest) with PageDataLoader
{
  def userId: String = journeyRequest.userId
  def email: Option[String] = journeyRequest.email

  def registrationNumber = journeyRequest.registrationNumber
  def userIsRegistered = journeyRequest.userIsRegistered

  val bpr = journeyRequest.bpr
  val businessType = journeyRequest.businessType

  def pageDataOpt[T](page: Page[T]): Option[T] = journeyRequest.pageDataOpt(page)

}

object SummaryAction {
  def apply(implicit save4LaterService: Save4LaterService, messagesApi: MessagesApi) =
    new UserAction() andThen new NoEnrolmentCheckAction() andThen new JourneyAction  andThen new SummaryAction
}

class SummaryAction(implicit val save4LaterService: Save4LaterService, val messagesApi: MessagesApi)
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
      Left(errorResultsPages(Results.BadRequest))
    }
  }

}