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

import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.{ActionRefiner, Result, Results, WrappedRequest}
import uk.gov.hmrc.fhregistrationfrontend.controllers.UnexpectedState
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

import scala.concurrent.Future

class AmendmentRequest[A](
  val registrationNumber: String,
  val  hasAmendmentInProgress: Boolean,
  request    : UserRequest[A]
) extends WrappedRequest[A](request) {

  def userId: String = request.userId
  def email: Option[String] = request.email
}

object AmendmentAction {
  def apply()(implicit save4LaterService: Save4LaterService, messagesApi: MessagesApi) = new UserAction andThen new AmendmentAction()

}

class AmendmentAction (implicit val save4LaterService: Save4LaterService, val messagesApi: MessagesApi)
    extends ActionRefiner[UserRequest, AmendmentRequest] with UnexpectedState
      with FrontendAction
{

  override protected def refine[A](request: UserRequest[A]): Future[Either[Result, AmendmentRequest[A]]] = {
    implicit val r = request
    save4LaterService
      .fetchIsAmendment(request.userId)
      .map { isAmendment ⇒
        request.registrationNumber match {
          case Some(registrationNumber) ⇒
            Right(new AmendmentRequest[A](registrationNumber, isAmendment getOrElse false, request))
          case None                     ⇒
            Logger.error(s"Not found: registration number. Is user enrolled?")
            Left(errorResultsPages(Results.BadRequest))
        }
      }
      .recover{ case t ⇒
        Logger.error(s"Could not access shortLivedCache", t)
        Left(errorResultsPages(Results.BadGateway))
      }

  }

}
