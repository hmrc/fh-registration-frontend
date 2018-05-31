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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import javax.inject.Inject

import play.api.mvc.Results
import uk.gov.hmrc.fhregistrationfrontend.actions.{Actions, EmailVerificationRequest}
import uk.gov.hmrc.fhregistrationfrontend.connectors.EmailVerificationConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.EmailVerificationForm.emailVerificationForm
import uk.gov.hmrc.fhregistrationfrontend.forms.models.EmailVerification
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.html.emailverification._

import scala.concurrent.Future

@Inject
class EmailVerificationController @Inject() (
  ds: CommonPlayDependencies,
  actions: Actions,
  emailVerificationConnector: EmailVerificationConnector,
  save4LaterService: Save4LaterService) extends AppController(ds)
{
  import actions._

  def contactEmail = emailVerificationAction { implicit request ⇒
    Ok(email_options(emailVerificationForm, false, request.candidateEmail, Navigation.noNavigation))
  }

  def forcedContactEmail = emailVerificationAction { implicit request ⇒
    Ok(email_options(emailVerificationForm, true, request.candidateEmail, Navigation.noNavigation))
  }

  def submitContactEmail = emailVerificationAction.async { implicit request ⇒
    emailVerificationForm.bindFromRequest() fold (
      formWithErrors ⇒ Future.successful(BadRequest(email_options(formWithErrors, false, request.candidateEmail, Navigation.noNavigation))),
      emailOptions ⇒ handleContactEmail(emailOptions)
    )
  }

  def submitForcedContactEmail = emailVerificationAction.async { implicit request ⇒
    emailVerificationForm.bindFromRequest() fold (
      formWithErrors ⇒ Future.successful(BadRequest(email_options(formWithErrors, true, request.candidateEmail, Navigation.noNavigation))),
      emailOptions ⇒ handleContactEmail(emailOptions)
    )
  }


  private def handleContactEmail(emailOptions: EmailVerification)(implicit request: EmailVerificationRequest[_]) = {
    emailVerificationConnector.requestVerification(emailOptions.email, emailHash(emailOptions.email)) flatMap { isVerified ⇒
      if (isVerified) {
        save4LaterService
          .saveVerifiedEmail(request.userId, emailOptions.email)
          .map { _ ⇒ Redirect(routes.Application.resumeForm())}
      } else {
        save4LaterService
          .savePendingEmail (request.userId, emailOptions.email)
          .map { _ ⇒ Redirect(routes.EmailVerificationController.emailVerificationStatus())}
      }
    }

  }

  def emailHash(email: String) = email.hashCode.toHexString.toUpperCase
  def hashMatches(email: String, hash: String) = emailHash(email) == hash

  def emailVerificationStatus = emailVerificationAction { implicit request ⇒
    (request.pendingEmail, request.verifiedEmail) match {
      case (Some(pendingEmail), None)    ⇒
        val form = emailVerificationForm.fill(EmailVerification(false, None, Some(pendingEmail)))
        Ok(email_pending_verification(form, Navigation.noNavigation, None))

      case (Some(pendingEmail), Some(verifiedEmail))
        if pendingEmail == verifiedEmail ⇒ Redirect(routes.Application.resumeForm())

      case (Some(pendingEmail), Some(_)) ⇒
        val form = emailVerificationForm.fill(EmailVerification(false, None, Some(pendingEmail)))
        Ok(email_pending_verification(form, Navigation.noNavigation, None))

      case (None, Some(_))               ⇒ Redirect(routes.Application.resumeForm())
      case (None, None)                  ⇒ Redirect(routes.EmailVerificationController.forcedContactEmail())

    }
  }

  def emailEdit = emailVerificationAction { implicit request ⇒
    (request.pendingEmail, request.verifiedEmail) match {
      case (Some(verifiedEmail), _) ⇒
        val form = emailVerificationForm.fill(EmailVerification(false, None, Some(verifiedEmail)))
        Ok(email_edit(form, Navigation.noNavigation))
      case (None, Some(pendingEmail)) ⇒
        val form = emailVerificationForm.fill(EmailVerification(false, None, Some(pendingEmail)))
        Ok(email_edit(form, Navigation.noNavigation))
      case _ ⇒
        errorHandler.errorResultsPages(Results.BadRequest)
    }

  }

  def emailChange = emailVerificationAction { implicit request ⇒
    (request.pendingEmail, request.verifiedEmail) match {
      case (Some(verifiedEmail), _) ⇒
        Ok(email_change_start(verifiedEmail, Navigation.noNavigation))
      case (None, Some(pendingEmail)) ⇒
        Ok(email_change_start(pendingEmail, Navigation.noNavigation))
      case _ ⇒
        errorHandler.errorResultsPages(Results.BadRequest)
    }
  }

  def emailVerified = emailVerificationAction { implicit request ⇒
    request.verifiedEmail match {
      case Some(verifiedEmail) ⇒ Ok(email_verified(verifiedEmail, Navigation.noNavigation))
      case None ⇒ errorHandler.errorResultsPages(Results.BadRequest)
    }
  }

  def emailVerify(token: String) = emailVerificationAction.async { implicit request ⇒
    request.pendingEmail match {
      case Some(pendingEmail) if hashMatches(pendingEmail, token) ⇒
        for {
          _ ←  save4LaterService saveVerifiedEmail (request.userId, pendingEmail)
          _ ← save4LaterService deletePendingEmail request.userId
        } yield {
          Redirect(routes.EmailVerificationController.emailVerified)
        }
      case Some(pendingEmail)                                     ⇒
        val form = emailVerificationForm.fill(EmailVerification(false, None, Some(pendingEmail)))
        Future successful Ok(email_pending_verification(form, Navigation.noNavigation, None))

      case None                                                   ⇒
        Future successful Redirect(routes.Application.resumeForm())
    }
  }

}
