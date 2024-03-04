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

import com.google.inject.Inject
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.controllers.routes
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._

import scala.concurrent.{ExecutionContext, Future}

class NextPageAction[T, V] @Inject()(pageId: String, sectionId: Option[String], journeys: Journeys)(
  implicit errorHandler: ErrorHandler,
  val executionContext: ExecutionContext)
    extends ActionRefiner[JourneyRequest, PageRequest] with FrontendAction {

  def getNextPageFromJourneysAndCurrentPageId(
    journeyType: JourneyType,
    journeyPages: JourneyPages,
    journeyState: JourneyState,
    pageId: String
  ): String =
    journeys.getNextPageFromJourneysAndCurrentPageId(journeyType, journeyPages, journeyState, pageId)

  override def refine[A](input: JourneyRequest[A]): Future[Either[Result, PageRequest[A]]] = {
    implicit val r: JourneyRequest[A] = input
    val newPageIdString =
      getNextPageFromJourneysAndCurrentPageId(input.journeyType, input.journeyPages, input.journeyState, pageId)

    Future.successful(Left(Redirect(routes.FormPageController.load(newPageIdString))))
  }
}
