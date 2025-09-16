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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.data.FormError
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions._
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.confirmation.WithdrawalConfirmation
import uk.gov.hmrc.fhregistrationfrontend.forms.confirmation.WithdrawalConfirmationForm.withdrawalConfirmationForm
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.WithdrawalReason
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.WithdrawalReasonForm.withdrawalReasonForm
import uk.gov.hmrc.fhregistrationfrontend.models.des
import uk.gov.hmrc.fhregistrationfrontend.services.SummaryConfirmationService
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.DesToForm
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import java.time.LocalDate
import java.util.Date
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Inject
class WithdrawalController @Inject() (
  ds: CommonPlayDependencies,
  val fhddsConnector: FhddsConnector,
  val desToForm: DesToForm,
  summaryConfirmationService: SummaryConfirmationService,
  cc: MessagesControllerComponents,
  actions: Actions,
  views: Views
)(implicit ec: ExecutionContext)
    extends AppController(ds, cc) with ContactEmailFunctions {

  import actions._
  val EmailSessionKey = "withdrawal_confirmation_email"
  val ProcessingTimestampSessionKey = "withdrawal_processing_timestamp"

  def startWithdraw: Action[AnyContent] = Action {
    Redirect(routes.WithdrawalController.reason)
  }

  def reason: Action[AnyContent] = enrolledUserAction { implicit request =>
    Ok(views.withdrawal_reason(withdrawalReasonForm))
  }

  def postReason: Action[AnyContent] = enrolledUserAction.async { implicit request =>
    withdrawalReasonForm
      .bindFromRequest()
      .fold(
        formWithError => Future successful BadRequest(views.withdrawal_reason(formWithError)),
        withdrawalReason =>
          summaryConfirmationService
            .saveWithdrawalReason(withdrawalReason)
            .map(_ => Redirect(routes.WithdrawalController.confirm))
      )
  }

  def withWithdrawalReason(f: EnrolledUserRequest[?] => WithdrawalReason => Future[Result]): Action[AnyContent] =
    enrolledUserAction.async { implicit request =>
      summaryConfirmationService.fetchWithdrawalReason() flatMap {
        case Some(reason) => f(request)(reason)
        case None         => Future successful errorHandler.errorResultsPages(Results.BadRequest)
      }
    }

  def confirm: Action[AnyContent] = withWithdrawalReason { implicit request => reason =>
    contactEmail map (email => Ok(views.withdrawal_confirm(withdrawalConfirmationForm, email)))
  }

  def postConfirmation: Action[AnyContent] = withWithdrawalReason { implicit request => reason =>
    withdrawalConfirmationForm
      .bindFromRequest()
      .fold(
        formWithError => {
          val errorsNew: List[FormError] = formWithError.errors.groupBy(_.key).map(x => x._2.head).toList
          val newFormErrors = formWithError.copy(errors = errorsNew)
          contactEmail map (email => BadRequest(views.withdrawal_confirm(newFormErrors, email)))
        },
        handleConfirmation(_, reason)
      )
  }

  private def handleConfirmation(confirmed: WithdrawalConfirmation, reason: WithdrawalReason)(implicit
    request: EnrolledUserRequest[?]
  ): Future[Result] =
    contactEmail flatMap (email =>
      if (confirmed.continue) {
        sendRequest(email.get, reason)
          .map { processingDate =>
            Redirect(routes.WithdrawalController.acknowledgment)
              .withSession(
                request.session
                  + (EmailSessionKey               -> email.get)
                  + (ProcessingTimestampSessionKey -> processingDate.getTime.toString)
              )

          }
      } else {
        Future successful Redirect(routes.Application.checkStatus())
      }
    )

  private def sendRequest(email: String, reason: WithdrawalReason)(implicit
    request: EnrolledUserRequest[?]
  ): Future[Date] = {
    val withdrawRequest = des.WithdrawalRequest(
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

  def acknowledgment: Action[AnyContent] = userAction { implicit request =>
    renderAcknowledgmentPage(using request) getOrElse errorHandler.errorResultsPages(Results.NotFound)
  }

  private def renderAcknowledgmentPage(implicit request: UserRequest[AnyContent]) =
    for {
      email     <- request.session get EmailSessionKey
      timestamp <- request.session get ProcessingTimestampSessionKey
      processingDate = new Date(timestamp.toLong)
    } yield Ok(views.withdrawal_acknowledgement(processingDate, email))
}
