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
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page.AnyPage
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future


class PageRequest[A](
  val journeyState: JourneyState,
  val journey: JourneyNavigation,
  p: AnyPage,
  request: UserRequest[A]) extends WrappedRequest[A](request)
{

  def page[T]: Page[T] = p.asInstanceOf[Page[T]]
  def userId: String = request.userId
}

object PageAction {
  def apply(pageId: String)(implicit save4LaterService: Save4LaterService) = UserAction andThen new PageAction(pageId, None)
  def apply(pageId: String, sectionId: Option[String])(implicit save4LaterService: Save4LaterService) = UserAction andThen new PageAction(pageId, sectionId)
}


class PageAction[T, V](pageId: String, sectionId: Option[String])(implicit save4LaterService: Save4LaterService) extends ActionRefiner[UserRequest, PageRequest]
  with FrontendAction
{

  override def refine[A](input: UserRequest[A]): Future[Either[Result, PageRequest[A]]] = {
    implicit val r: UserRequest[A] = input
    val journeyPages = Journeys.limitedCompanyPages

    val result: EitherT[Future, Result, PageRequest[A]] = for {
      page <- loadPage(input, journeyPages).toEitherT[Future]
      cacheMap ← EitherT(loadCacheMap)
      journeyState = loadJourneyState(journeyPages, cacheMap)
      _ ← accessiblePage(page, journeyState).toEitherT[Future]
      pageWithData = loadPageData(cacheMap, page)
      pageWithSection ← loadPageSection(pageWithData).toEitherT[Future]
      journeyNavigation = loadJourneyNavigation(journeyPages, journeyState)
    } yield {
      new PageRequest(
        journeyState,
        journeyNavigation,
        pageWithSection,
        input
      )
    }
    result.value
  }

  def accessiblePage(page: AnyPage, state: JourneyState):  Either[Result, Boolean] = {
    if (state.isPageComplete(page) || state.nextPageToComplete() == Some(page.id)) {
      Right(true)
    } else {
      Left(NotFound("Not found"))
    }
  }

  def loadPageData(cacheMap: CacheMap, page: Page[T]) = {
    cacheMap.getEntry[T](page.id)(page.format).fold (page) { data ⇒
      page withData data
    }
  }

  def loadPageSection(page: Page[T]): Either[Result, Page[T]] = {
    if (page.withSubsection isDefinedAt sectionId)
      Right(page withSubsection sectionId)
    else
      Left(NotFound("Not found"))
  }

  def loadPage[A](request: UserRequest[A], journeyPages: JourneyPages): Either[Result, Page[T]] =
    journeyPages.get[T](pageId) match {
      case Some(page) ⇒ Right(page)
      case None       ⇒ Left(NotFound("Not Found"))
    }

  def loadJourneyState(journeyPages: JourneyPages, cacheMap: CacheMap): JourneyState = {
    Journeys.journeyState(journeyPages, cacheMap)
  }

  def loadCacheMap(implicit request: UserRequest[_]): Future[Either[Result, CacheMap]] = {
    save4LaterService.shortLivedCache.fetch(request.userId) map {
      case Some(cacheMap) ⇒ Right(cacheMap)
      case None ⇒ Left(NotFound)
    } recover { case t ⇒
      Left(BadGateway)
    }
  }

  def loadJourneyNavigation(journeyPages: JourneyPages, state: JourneyState) = {
    if (state.isComplete)
      Journeys.summaryJourney(journeyPages)
    else
      Journeys.linearJourney(journeyPages)
  }


}
