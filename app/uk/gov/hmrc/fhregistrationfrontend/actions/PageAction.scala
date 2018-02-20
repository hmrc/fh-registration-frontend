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
import play.api.mvc.{ActionRefiner, Result, WrappedRequest}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

import scala.concurrent.Future


class PageRequest[A](
  val journeyState: JourneyState,
  val journey: JourneyNavigation,
  p: Page[_],
  request: UserRequest[A]) extends WrappedRequest[A](request)
{

  def page[T] = p.asInstanceOf[Page[T]]
  def userId = request.userId
}

object PageAction {
  def apply(pageId: String)(implicit save4LaterService: Save4LaterService) = (UserAction andThen new PageAction(pageId))
}

class PageAction[T](pageId: String)(implicit save4LaterService: Save4LaterService) extends ActionRefiner[UserRequest, PageRequest]
  with FrontendAction
{

  val journeyPages = Journeys.limitedCompanyPages

  override def refine[A](input: UserRequest[A]): Future[Either[Result, PageRequest[A]]] = {
    implicit val r = input

    val result: EitherT[Future, Result, PageRequest[A]] = for {
      page <- loadPage(input).toEitherT[Future]
      journeyState ← EitherT(loadJourneyState(input))
      _ ← accessiblePage(page, journeyState).toEitherT[Future]
      journeyNavigation = loadJourneyNavigation(journeyState)
    } yield {
      new PageRequest(
        journeyState,
        journeyNavigation,
        page,
        input
      )
    }
    result.value
  }

  def accessiblePage(page: Page[_], state: JourneyState):  Either[Result, Boolean] = {
    if (state.isPageComplete(page) || state.nextPageToComplete() == Some(page.id)) {
      Right(true)
    } else {
      Left(NotFound("Not found"))
    }
  }


  def loadPage[A](request: UserRequest[A]): Either[Result, Page[_]] =
    journeyPages.get(pageId) match {
      case Some(page) ⇒ Right(page)
      case None       ⇒ Left(NotFound)
    }

  def loadJourneyState(implicit request: UserRequest[_]): Future[Either[Result, JourneyState]] = {
    save4LaterService.shortLivedCache.fetch(request.userId) map {
      case Some(cacheMap) ⇒ Right(Journeys.limitedCompanyJourneyState(journeyPages, cacheMap))
      case None ⇒ Left(NotFound)
    } recover { case t ⇒
      Left(BadGateway)
    }
  }

  def loadJourneyNavigation(state: JourneyState) = {
    if (state.isComplete)
      Journeys.summaryJourney(journeyPages)
    else
      Journeys.linearJourney(journeyPages)
  }


}
