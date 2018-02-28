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
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.models.Enrolments
import uk.gov.hmrc.auth.core.retrieve.Retrievals.{internalId, email, allEnrolments}
import uk.gov.hmrc.auth.core.retrieve.~

import uk.gov.hmrc.auth.core.{AuthorisationException, AuthorisedFunctions, NoActiveSession, PlayAuthConnector}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAuthConnector
import uk.gov.hmrc.fhregistrationfrontend.connectors.ExternalUrls.{continueUrl, getString, ggLoginUrl}

import scala.concurrent.Future

class UserRequest[A](val userId: String, val email: Option[String], val registrationNumber: Option[String], request: Request[A])
  extends WrappedRequest(request) {
}

object UserAction extends ActionBuilder[UserRequest]
  with ActionRefiner[Request, UserRequest]
  with FrontendAction
  with AuthorisedFunctions {
  override def authConnector: PlayAuthConnector = FrontendAuthConnector

  override protected def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] = {
    implicit val r = request

    authorised().retrieve(internalId and email and allEnrolments) {
      case Some(id) ~ anEmail ~ enrolments ⇒
        val fhddsRegistrationNumber = for {
          enrolment ← enrolments getEnrolment Enrolments.serviceName
          identifier ← enrolment getIdentifier Enrolments.identifierName
        } yield {
          identifier.value
        }

        Future successful Right(new UserRequest(id, anEmail, fhddsRegistrationNumber, request))
      case _     ⇒
        throw AuthorisationException.fromString("Can not find user id")

    } recover { case e ⇒
      Left(handleFailure(e))
    }
  }

  def handleFailure(e: Throwable)(implicit request: Request[_]): Result =
    e match {
      case x: NoActiveSession      ⇒
        Logger.warn(s"could not authenticate user due to: No Active Session " + x)

        val ggRedirectParms = Map(
          "continue" -> Seq(continueUrl),
          "origin" -> Seq(getString("appName"))
        )

        Redirect(ggLoginUrl, ggRedirectParms)
      case e: AuthorisationException ⇒
        Unauthorized
      case ex                      ⇒
        Logger.warn(s"could not authenticate user due to: $ex")
        InternalServerError(s"$ex")
    }
}
