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

import models.Mode
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.fhregistrationfrontend.config.{ErrorHandler, FrontendAppConfig}
import uk.gov.hmrc.fhregistrationfrontend.connectors.{ExternalUrls, FhddsConnector}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Journeys
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Actions @Inject()(
  externalUrls: ExternalUrls,
  fhddsConnector: FhddsConnector,
  sessionCache: SessionRepository,
  frontendAppConfig: FrontendAppConfig,
  cc: ControllerComponents,
  journeys: Journeys
)(
  implicit val authConnector: AuthConnector,
  save4LaterService: Save4LaterService,
  errorHandler: ErrorHandler,
  ec: ExecutionContext) {

  def userAction: ActionBuilder[UserRequest, AnyContent] = UserAction(externalUrls, errorHandler, cc)
  def dataRetrievalAction =
    userAction andThen newBusinessPartnersFlowEnabledAction andThen new DataRetrievedAction(sessionCache)
  def dataRequiredAction(index: Int, mode: Mode) =
    userAction andThen newBusinessPartnersFlowEnabledAction andThen new DataRetrievedAction(sessionCache) andThen new DataRequiredAction(
      ec,
      index,
      mode)

  def notAdminUser = userAction andThen new NotAdminUserFilter
  def noPendingSubmissionFilter = userAction andThen new NoPendingSubmissionFilter(fhddsConnector)
  def emailVerificationAction = new UserAction(externalUrls, errorHandler, cc) andThen new EmailVerificationAction

  def startAmendmentAction = userAction andThen new StartAmendmentAction(fhddsConnector)
  def startVariationAction = userAction andThen new StartVariationAction(fhddsConnector)
  def enrolledUserAction = userAction andThen new EnrolledUserAction
  def journeyAction = userAction andThen new JourneyAction(journeys)
  def pageAction(pageId: String) = journeyAction andThen new PageAction(pageId, None, journeys)

  def newApplicationAction =
    noPendingSubmissionFilter andThen notAdminUser andThen new NewApplicationAction(fhddsConnector)

  def pageAction(pageId: String, sectionId: Option[String]) =
    journeyAction andThen new PageAction(pageId, sectionId, journeys)

  def summaryAction =
    userAction andThen journeyAction andThen new SummaryAction

  def newBusinessPartnersFlowEnabledAction: ActionRefiner[UserRequest, UserRequest] =
    new ActionRefiner[UserRequest, UserRequest] {
      override protected def refine[A](request: UserRequest[A]): Future[Either[Result, UserRequest[A]]] =
        if (frontendAppConfig.newBusinessPartnerPagesEnabled) {
          Future.successful(Right(request))
        } else {
          Future.successful(Left(errorHandler.errorResultsPages(Results.NotFound)(request)))
        }

      override protected def executionContext: ExecutionContext = ec
    }
}
