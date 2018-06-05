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

import java.time.LocalDate
import java.util.Date

import javax.inject.Inject
import org.joda.time.DateTime
import play.api.mvc.{Action, AnyContent, Result, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions._
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.ConfirmationForm.confirmationForm
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.{Confirmation, DeregistrationReason}
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.DeregistrationReasonForm.deregistrationReasonForm
import uk.gov.hmrc.fhregistrationfrontend.models.des
import uk.gov.hmrc.fhregistrationfrontend.services.KeyStoreService
import uk.gov.hmrc.fhregistrationfrontend.views.html.deregistration.{deregistration_acknowledgement, deregistration_confirm, deregistration_reason}

import scala.concurrent.Future

@Inject
class DeregistrationController @Inject()(
                                      ds               : CommonPlayDependencies,
                                      fhddsConnector   : FhddsConnector,
                                      keyStoreService  : KeyStoreService,
                                      actions: Actions
                                    ) extends AppController(ds) {

  import actions._
  val EmailSessionKey = "deregistration_confirmation_email"
  val ProcessingTimestampSessionKey = "deregistration_processing_timestamp"


  def startDeregister = Action {
    Redirect(routes.DeregistrationController.reason())
  }

  def reason = enrolledUserAction { implicit request ⇒
    Ok(deregistration_reason(deregistrationReasonForm))
  }

  def postReason = enrolledUserAction.async { implicit request ⇒
    deregistrationReasonForm.bindFromRequest().fold(
      formWithError ⇒ Future successful BadRequest(deregistration_reason(formWithError)),
      deregistrationReason ⇒
        keyStoreService
          .saveDeregistrationReason(deregistrationReason)
          .map(_ ⇒ Redirect(routes.DeregistrationController.confirm()))
    )
  }

  def withDeregistrationReason(f: EnrolledUserRequest[_] ⇒ DeregistrationReason ⇒ Future[Result]) =
    enrolledUserAction.async { implicit request ⇒
      keyStoreService.fetchDeregistrationReason() flatMap {
        case Some(reason) ⇒ f(request)(reason)
        case None ⇒ Future successful errorHandler.errorResultsPages(Results.BadRequest)
      }
    }

  def confirm = withDeregistrationReason {
    implicit request ⇒ reason ⇒
      keyStoreService.fetchDeregistrationReason().map {
        case Some(_) ⇒ Ok(deregistration_confirm(confirmationForm, request.email))
        case None    ⇒ errorHandler.errorResultsPages(Results.BadRequest)
      }
  }


  def postConfirmation = withDeregistrationReason {
    implicit request ⇒
      reason ⇒
        confirmationForm.bindFromRequest().fold(
          formWithError ⇒ Future successful BadRequest(deregistration_confirm(formWithError, request.email)),
          handleConfirmation(_, reason)
        )
  }

  private def handleConfirmation(confirmed: Confirmation, reason: DeregistrationReason)(implicit request: EnrolledUserRequest[_]) = {
    if (confirmed.continue) {
      sendRequest(confirmed.email.get, reason)
        .map { processingDate ⇒
          Redirect(routes.DeregistrationController.acknowledgment())
            .withSession(
              request.session
                + (EmailSessionKey → confirmed.email.get)
                + (ProcessingTimestampSessionKey → processingDate.getTime.toString))

        }
    } else {
      Future successful Redirect(routes.Application.checkStatus)
    }
  }

  private def sendRequest(email: String, reason: DeregistrationReason)(implicit request: EnrolledUserRequest[_]): Future[Date] = {
    val deregistrationRequest =  des.DeregistrationRequest(
      email,
      des.Deregistration(
        LocalDate.now(),
        reason.deregistrationReason.toString,
        reason.deregistrationReasonOther
      )
    )
    fhddsConnector
      .deregister(request.registrationNumber, deregistrationRequest)
  }

  def acknowledgment = userAction { implicit request ⇒
    renderAcknowledgmentPage(request) getOrElse errorHandler.errorResultsPages(Results.NotFound)
  }

  private def renderAcknowledgmentPage(implicit request: UserRequest[AnyContent]) = {
    for {
      email ← request.session get EmailSessionKey
      timestamp ← request.session get ProcessingTimestampSessionKey
      processingDate = new DateTime(timestamp.toLong)
    } yield {
      Ok(deregistration_acknowledgement(processingDate, email))
    }
  }
}
