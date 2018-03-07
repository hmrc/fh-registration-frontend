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

import play.api.mvc._
import play.api.Logger
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{JourneyPages, Page, PageDataLoader}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.http.cache.client.CacheMap
import cats.data.EitherT
import cats.implicits._
import play.api.i18n.Messages
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails

import scala.concurrent.Future

class SummaryRequest[A](
  cacheMap: CacheMap,
  request: UserRequest[A],
  val bpr: BusinessRegistrationDetails,
  val businessType: BusinessType
) extends WrappedRequest[A](request) with PageDataLoader
{
  def userId: String = request.userId
  def email: Option[String] = request.email

  def registrationNumber = request.registrationNumber
  def userIsRegistered = request.userIsRegistered

  def pageData[T](page: Page[T]): T =
    cacheMap.getEntry[T](page.id)(page.format).get

}

object SummaryAction {
  def apply(implicit save4LaterService: Save4LaterService, messages: Messages, request: Request[_]) = UserAction andThen new SummaryAction
}

class SummaryAction(implicit val save4LaterService: Save4LaterService, val messages: Messages, val request: Request[_])
  extends JourneyAction
    with ActionRefiner[UserRequest, SummaryRequest]{

  override protected def refine[A](input: UserRequest[A]): Future[Either[Result, SummaryRequest[A]]] = {
    implicit val r: UserRequest[A] = input
    val result: EitherT[Future, Result, SummaryRequest[A]] = for {
      cacheMap ← EitherT(loadCacheMap)
      journeyPages ← getJourneyPages(cacheMap).toEitherT[Future]
      _ ← journeyIsComplete(journeyPages, cacheMap, messages, request).toEitherT[Future]
      bpr ← findBpr(cacheMap).toEitherT[Future]
      bt ← getBusinessType(cacheMap).toEitherT[Future]
    } yield {
      new SummaryRequest[A](cacheMap, input, bpr, bt)
    }

    result.value
  }

  def journeyIsComplete(journeyPages: JourneyPages, cacheMap: CacheMap, messages: Messages, request: Request[_]): Either[Result, Boolean] = {
    if(loadJourneyState(journeyPages, cacheMap).isComplete)
      Right(true)
    else
      Logger.error(s"Bad Request")
      Left(errorResultsPages(Results.BadRequest)(request,messages))
  }

}