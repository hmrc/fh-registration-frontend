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
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyState
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

import scala.concurrent.Future

class ResumeJourneyRequest[A](
  request: UserRequest[A],
  val journeyState: JourneyState
) extends WrappedRequest[A](request)

object ResumeJourneyAction {
  def apply(implicit save4LaterService: Save4LaterService) = UserAction andThen new ResumeJourneyAction
}


class ResumeJourneyAction(implicit val save4LaterService: Save4LaterService)
  extends ActionRefiner[UserRequest, ResumeJourneyRequest]
  with FrontendAction
  with JourneyAction
{
  override protected def refine[A](request: UserRequest[A]): Future[Either[Result, ResumeJourneyRequest[A]]] = {
    implicit val r: UserRequest[A] = request
    val result: EitherT[Future, Result, ResumeJourneyRequest[A]] = for {
      cacheMap ← EitherT(loadCacheMap)
      journeyPages ← getJourneyPages(cacheMap).toEitherT[Future]
      journeyState = loadJourneyState(journeyPages, cacheMap)
    } yield {
      new ResumeJourneyRequest[A](request, journeyState)
    }

    result.value
  }
}

