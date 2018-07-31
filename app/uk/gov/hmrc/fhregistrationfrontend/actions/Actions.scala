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

import javax.inject.Inject

import play.api.mvc.ActionBuilder
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.connectors.{ExternalUrls, FhddsConnector}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

class Actions @Inject() (
  externalUrls: ExternalUrls,
  fhddsConnector: FhddsConnector
)(implicit val authConnector: AuthConnector,
  save4LaterService: Save4LaterService,
  errorHandler: ErrorHandler) {


  def userAction: ActionBuilder[UserRequest] = new UserAction(externalUrls, errorHandler)

  def noPendingSubmissionFilter = userAction andThen new NoPendingSubmissionFilter(fhddsConnector)
  def emailVerificationAction = new UserAction(externalUrls, errorHandler) andThen new EmailVerificationAction

  def startAmendmentAction = userAction andThen new StartAmendmentAction(fhddsConnector)
  def startVariationAction = userAction andThen new StartVariationAction(fhddsConnector)
  def enrolledUserAction = userAction andThen new EnrolledUserAction
  def journeyAction = userAction andThen new JourneyAction
  def pageAction(pageId: String) = journeyAction andThen new PageAction(pageId, None)

  def newApplicationAction = noPendingSubmissionFilter andThen new NewApplicationAction(fhddsConnector)

  def pageAction(pageId: String, sectionId: Option[String]) =
    journeyAction andThen new PageAction(pageId, sectionId)

  def summaryAction =
    userAction andThen journeyAction andThen new SummaryAction



}