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

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.fhregistration.models.fhdds.{SubmissionRequest, SubmissionResponse}
import uk.gov.hmrc.fhregistrationfrontend.actions.{SummaryAction, SummaryRequest, UserAction, UserRequest}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.DeclarationForm.declarationForm
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{Journeys, PageDataLoader}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessType, Declaration}
import uk.gov.hmrc.fhregistrationfrontend.models.des.SubScriptionCreate
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.{DesToForm, Diff, FormToDes, FormToDesImpl}
import uk.gov.hmrc.fhregistrationfrontend.services.{KeyStoreService, Save4LaterService}
import uk.gov.hmrc.fhregistrationfrontend.views.html.{acknowledgement_page, declaration}

import scala.concurrent.Future

@Inject
class DeclarationController @Inject()(
  ds            : CommonPlayDependencies,
  links         : ExternalUrls,
  desToForm     : DesToForm,
  fhddsConnector: FhddsConnector,
  keyStoreService      : KeyStoreService
)(implicit save4LaterService: Save4LaterService) extends AppController(ds) with SummaryFunctions {

  val emailSessionKey = "declaration_email"
  val processingTimestampSessionKey = "declaration_processing_timestamp"
  val formToDes: FormToDes = new FormToDesImpl()

  def showDeclaration() = SummaryAction(save4LaterService, messagesApi) { implicit request ⇒
    Ok(declaration(declarationForm, request.email, request.bpr))
  }

  def showAcknowledgment() = UserAction()(messagesApi) { implicit request ⇒
    renderAcknowledgmentPage getOrElse errorResultsPages(Results.NotFound)
  }

  private def renderAcknowledgmentPage(implicit request: UserRequest[_]): Option[Result] = {
    for {
      email ← request.session get emailSessionKey
      timestamp ← request.session get processingTimestampSessionKey
      processingDate = new Date(timestamp.toLong)
    } yield {
      Ok(acknowledgement_page(processingDate, email))
    }
  }

  def submitForm() = SummaryAction(save4LaterService, messagesApi).async { implicit request ⇒
    val form = declarationForm.bindFromRequest()
    form.fold(
      formWithErrors => Future successful BadRequest(declaration(formWithErrors, request.email, request.bpr)),
      usersDeclaration => {
        sendSubscription(usersDeclaration).fold(
          error ⇒ Future successful BadRequest(declaration(form, request.email, request.bpr, Some(false))),
          _.flatMap { response ⇒
            keyStoreService.saveSummaryFormPrint(getSummaryHtml(request, forPrint = true, timeStamp=Some(response.processingDate)).toString())
              .map(_ ⇒ true)
              .recover{case _ ⇒ false}
              .map { pdfSaved ⇒
                Redirect(routes.DeclarationController.showAcknowledgment())
                  .withSession(request.session
                    + (emailSessionKey → usersDeclaration.email)
                    + (processingTimestampSessionKey → response.processingDate.getTime.toString))
              }
          }
        )
      }
    )
  }

  private def sendSubscription(declaration: Declaration)(implicit request: SummaryRequest[_]):  Either[String, Future[SubmissionResponse]] = {
    val submissionResult =
      if (request.userIsRegistered) {
        amendedSubmission(declaration)
      } else {
        createSubmission(declaration)
      }

    submissionResult.right map { submissionResponse ⇒
      submissionResponse onSuccess {case _ ⇒ save4LaterService.removeUserData(request.userId) }
      submissionResponse
    }
  }

  def createSubmission(declaration: Declaration)(implicit request: SummaryRequest[_]): Either[String, Future[SubmissionResponse]] = {
    val subscription = getSubscriptionForDes(formToDes, declaration, request)
    val payload = SubScriptionCreate(subscription)

    val submissionRequest = SubmissionRequest(
      declaration.email,
      Json toJson payload
    )

    Right(
      fhddsConnector.createSubmission(request.bpr.safeId.get, submissionRequest))

  }

  def amendedSubmission(declaration: Declaration)(implicit request: SummaryRequest[_]): Either[String, Future[SubmissionResponse]] = {
    val newDesDeclaration = formToDes.declaration(declaration)
    val prevDesDeclaration = request.journeyRequest.displayDeclaration.get

    if (prevDesDeclaration == newDesDeclaration && request.journeyRequest.hasAmendments == Some(false))
      Left("no.changes")
    else {
      val subscription = getSubscriptionForDes(
        formToDes.withModificationFlags(true, Some(LocalDate.now)),
        declaration,
        request
      )

      val prevSubscription = getSubscriptionForDes(
        formToDes.withModificationFlags(false, None),
        desToForm.declaration(prevDesDeclaration),
        request.journeyRequest.displayPageDataLoader
      )

      val changeIndicators = Diff.changeIndicators(prevSubscription, subscription)
      val payload = SubScriptionCreate.subscriptionAmend(changeIndicators, subscription)
      val submissionRequest = SubmissionRequest(
        declaration.email,
        Json toJson payload
      )

      Right(
        fhddsConnector.amendSubmission(request.registrationNumber.get, submissionRequest)
      )
    }
  }

  private def getSubscriptionForDes(formToDes: FormToDes, d: Declaration, pageDataLoader: PageDataLoader)(implicit request: SummaryRequest[_]) = {
    request.businessType match {
      case BusinessType.CorporateBody ⇒ formToDes limitedCompanySubmission(request.bpr, Journeys ltdApplication pageDataLoader, d)
      case BusinessType.SoleTrader    ⇒ formToDes soleProprietorCompanySubmission(request.bpr, Journeys soleTraderApplication pageDataLoader, d)
      case BusinessType.Partnership   ⇒ formToDes partnership(request.bpr, Journeys partnershipApplication pageDataLoader, d)
    }
  }

}