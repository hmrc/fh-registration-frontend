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

import play.api.i18n.MessagesApi
import play.api.mvc.{ActionRefiner, Result, Results}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress

import scala.concurrent.Future

object ContinueWithBprAction {
  def apply(fhddsConnector: FhddsConnector)(implicit messagesApi: MessagesApi) = new UserAction() andThen new ContinueWithBprAction(fhddsConnector)(messagesApi)
}

class ContinueWithBprAction(fhddsConnector: FhddsConnector) (val messagesApi: MessagesApi)
  extends ActionRefiner[UserRequest, UserRequest] with FrontendAction {

  override protected def refine[A](request: UserRequest[A]): Future[Either[Result, UserRequest[A]]] = {

    implicit val r = request

    if (r.userIsRegistered)
      Future successful Left(errorResultsPages(Results.BadRequest))
    else {
      fhddsConnector
        .getEnrolmentProgress
        .map {
          case EnrolmentProgress.Pending ⇒ Left(errorResultsPages(Results.BadRequest))
          case _ ⇒ Right(r)
        }
    }
  }
}
