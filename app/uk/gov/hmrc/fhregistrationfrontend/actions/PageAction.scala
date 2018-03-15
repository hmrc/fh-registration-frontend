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
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page.AnyPage
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class PageRequest[A](
  val journey: JourneyNavigation,
  p: AnyPage,
  request: JourneyRequest[A]) extends WrappedRequest[A](request)
{

  def page[T]: Page[T] = p.asInstanceOf[Page[T]]
  def userId: String = request.userId

  val journeyState = request.journeyState
  def lastUpdateTimestamp = request.lastUpdateTimestamp
}

object PageAction {
  def apply(pageId: String)(implicit save4LaterService: Save4LaterService, messagesApi: MessagesApi) =
    JourneyAction() andThen new PageAction(pageId, None)

  def apply(pageId: String, sectionId: Option[String])(implicit save4LaterService: Save4LaterService, messagesApi: MessagesApi) =
    JourneyAction() andThen new PageAction(pageId, sectionId)
}


//TODO all exceptional results need to be reviewed
class PageAction[T, V](pageId: String, sectionId: Option[String])
  (implicit val save4LaterService: Save4LaterService, val messagesApi: MessagesApi)
  extends ActionRefiner[JourneyRequest, PageRequest] with FrontendAction {

  override def refine[A](input: JourneyRequest[A]): Future[Either[Result, PageRequest[A]]] = {
    implicit val r: JourneyRequest[A] = input

    val result: EitherT[Future, Result, PageRequest[A]] = for {

      page <- loadPage(input).toEitherT[Future]
      _ ← accessiblePage(page, input.journeyState).toEitherT[Future]
      pageWithData = loadPageWithData(input, page)
      pageWithSection ← loadPageSection(pageWithData).toEitherT[Future]
      journeyNavigation = loadJourneyNavigation(input.journeyPages, input.journeyState)
    } yield {
      new PageRequest(
        journeyNavigation,
        pageWithSection,
        input
      )
    }
    result.value
  }

  def accessiblePage(page: AnyPage, state: JourneyState)(implicit request: Request[_]):  Either[Result, Boolean] = {
    if (state.isPageComplete(page) || state.nextPageToComplete() == Some(page.id)) {
      Right(true)
    } else {
      Logger.error(s"Not found")
      Left(errorResultsPages(Results.NotFound))
    }
  }

  def loadPageData(cacheMap: CacheMap, page: Page[T]): Option[T] =
    cacheMap.getEntry[T](page.id)(page.format)


  def loadPageWithData(pageDataLoader: PageDataLoader, page: Page[T]) = {
    pageDataLoader.pageDataOpt(page).fold (page) { data ⇒
      page withData data
    }
  }

  def loadPageSection(page: Page[T])(implicit request: Request[_]): Either[Result, Page[T]] = {
    if (page.withSubsection isDefinedAt sectionId)
      Right(page withSubsection sectionId)
    else {
      Logger.error(s"Not found")
      Left(errorResultsPages(Results.NotFound))
    }
  }

  def loadPage[A](request: JourneyRequest[A])(implicit messages: Messages): Either[Result, Page[T]] =
    request.journeyState.get[T](pageId) match {
      case Some(page) ⇒ Right(page)
      case None       ⇒
        Logger.error(s"Not found")
        Left(errorResultsPages(Results.NotFound)(request,messages))
    }

  def loadJourneyNavigation(journeyPages: JourneyPages, state: JourneyState) = {
    if (state.isComplete)
      Journeys.summaryJourney(journeyPages)
    else
      Journeys.linearJourney(journeyPages)
  }


}
