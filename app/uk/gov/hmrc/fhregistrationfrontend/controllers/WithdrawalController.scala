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
import uk.gov.hmrc.fhregistrationfrontend.actions.{EnrolledUserAction, EnrolledUserRequest, UserAction, UserRequest}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.ConfirmationForm.confirmationForm
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.{Confirmation, WithdrawalReason}
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.WithdrawalReasonForm.withdrawalReasonForm
import uk.gov.hmrc.fhregistrationfrontend.models.des
import uk.gov.hmrc.fhregistrationfrontend.services.KeyStoreService
import uk.gov.hmrc.fhregistrationfrontend.views.html.withdrawals.{withdrawal_acknowledgement, withdrawal_confirm, withdrawal_reason}

import scala.concurrent.Future

@Inject
class WithdrawalController @Inject()(
  ds               : CommonPlayDependencies,
  fhddsConnector   : FhddsConnector,
  keyStoreService      : KeyStoreService
) extends AppController(ds) {


  val EmailSessionKey = "withdrawal_confirmation_email"
  val ProcessingTimestampSessionKey = "withdrawal_processing_timestamp"


  def startWithdraw = Action {
    Redirect(routes.WithdrawalController.reason())
  }

  def reason = EnrolledUserAction()(messagesApi) { implicit request ⇒
      Ok(withdrawal_reason(withdrawalReasonForm))
  }

  def postReason = EnrolledUserAction().async { implicit request ⇒
    withdrawalReasonForm.bindFromRequest().fold(
      formWithError ⇒ Future successful BadRequest(withdrawal_reason(formWithError)),
      withdrawalReason ⇒
        keyStoreService
          .saveWithdrawalReason(withdrawalReason)
          .map(_ ⇒ Redirect(routes.WithdrawalController.confirm()))
    )
  }

  def withWithdrawalReason(f: EnrolledUserRequest[_] ⇒ WithdrawalReason ⇒ Future[Result]) =
    EnrolledUserAction().async { implicit request ⇒
      keyStoreService.fetchWithdrawalReason() flatMap {
        case Some(reason) ⇒ f(request)(reason)
        case None ⇒ Future successful errorResultsPages(Results.BadRequest)
      }
    }

  def confirm = withWithdrawalReason {
    implicit request ⇒ reason ⇒
        keyStoreService.fetchWithdrawalReason().map {
          case Some(_) ⇒ Ok(withdrawal_confirm(confirmationForm, request.email))
          case None    ⇒ errorResultsPages(Results.BadRequest)
        }
  }


  def postConfirmation = withWithdrawalReason {
    implicit request ⇒
      reason ⇒
        confirmationForm.bindFromRequest().fold(
          formWithError ⇒ Future successful BadRequest(withdrawal_confirm(formWithError, request.email)),
          handleConfirmation(_, reason)
        )
  }

  private def handleConfirmation(confirmed: Confirmation, reason: WithdrawalReason)(implicit request: EnrolledUserRequest[_]) = {
    if (confirmed.continue) {
      sendRequest(confirmed.email.get, reason)
        .map { processingDate ⇒
          Redirect(routes.WithdrawalController.acknowledgment())
            .withSession(
              request.session
                + (EmailSessionKey → confirmed.email.get)
                + (ProcessingTimestampSessionKey → processingDate.getTime.toString))

        }
    } else {
      Future successful Redirect(routes.Application.checkStatus)
    }
  }

  private def sendRequest(email: String, reason: WithdrawalReason)(implicit request: EnrolledUserRequest[_]): Future[Date] = {
    val withdrawRequest =  des.WithdrawalRequest(
      email,
      des.Withdrawal(
        LocalDate.now(),
        reason.withdrawalReason.toString,
        reason.withdrawalReasonOther
      )
    )
    fhddsConnector
      .withdraw(request.registrationNumber, withdrawRequest)
  }

  def acknowledgment = UserAction()(messagesApi) { implicit request ⇒
    renderAcknowledgmentPage(request) getOrElse errorResultsPages(Results.NotFound)
  }

  private def renderAcknowledgmentPage(implicit request: UserRequest[AnyContent]) = {
    for {
      email ← request.session get EmailSessionKey
      timestamp ← request.session get ProcessingTimestampSessionKey
      processingDate = new DateTime(timestamp.toLong)
    } yield {
      Ok(withdrawal_acknowledgement(processingDate, email))
    }
  }
}
