/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Retrievals.{allEnrolments, email, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.connectors.ExternalUrls
import uk.gov.hmrc.fhregistrationfrontend.models.Enrolments

import scala.concurrent.Future

class UserRequest[A](val userId: String, val ggEmail: Option[String], val registrationNumber: Option[String], request: Request[A])
  extends WrappedRequest(request) {
  def userIsRegistered = registrationNumber.isDefined
}

class UserAction @Inject()(
  externalUrls: ExternalUrls,
  errorHandler: ErrorHandler
)(implicit override val authConnector: AuthConnector) extends ActionBuilder[UserRequest]
  with ActionRefiner[Request, UserRequest]
  with FrontendAction
  with AuthorisedFunctions {

  override protected def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] = {
    implicit val r = request

    authorised(AuthProviders(GovernmentGateway)).retrieve(internalId and email and allEnrolments) {
      case Some(id) ~ anEmail ~ enrolments ⇒
        val fhddsRegistrationNumbers = for {
          enrolment ← enrolments.enrolments
          if enrolment.key equalsIgnoreCase Enrolments.serviceName

          identifier ← enrolment.identifiers
          if identifier.key equalsIgnoreCase Enrolments.identifierName
          if identifier.value.slice(2, 4) == "FH"

        } yield {
          identifier.value
        }

        if (fhddsRegistrationNumbers.size > 1)
          Future successful Left(errorHandler.applicationError)
        else
          Future successful Right(new UserRequest(id, anEmail, fhddsRegistrationNumbers.headOption, request))
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
          "continue" -> Seq(externalUrls.continueUrl),
          "origin" -> Seq(externalUrls.ggOrigin)
        )

        Redirect(externalUrls.ggLoginUrl, ggRedirectParms)
      case e: AuthorisationException ⇒
        Unauthorized
      case ex                      ⇒
        Logger.warn(s"could not authenticate user due to: $ex")
        InternalServerError(s"$ex")
    }
}
