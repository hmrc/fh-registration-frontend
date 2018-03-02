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
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistration.models.fhdds.SubmissionRequest
import uk.gov.hmrc.fhregistrationfrontend.actions.{SummaryAction, SummaryRequest, UserAction}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.DeclarationForm.declarationForm
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Journeys
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessType, Declaration}
import uk.gov.hmrc.fhregistrationfrontend.models.des.{SubScriptionCreate, Subscription}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

import uk.gov.hmrc.fhregistrationfrontend.services.mapping.{DesToForm, Diff, FormToDes}
import uk.gov.hmrc.fhregistrationfrontend.views.html.{acknowledgement, declaration}

import uk.gov.hmrc.fhregistrationfrontend.services.mapping.FormToDes
import uk.gov.hmrc.fhregistrationfrontend.views.html.{acknowledgement_page, declaration}

import scala.concurrent.Future

@Inject
class DeclarationController @Inject()(
  ds            : CommonPlayDependencies,
  messagesApi   : play.api.i18n.MessagesApi,
  links         : ExternalUrls,
  formToDes     : FormToDes,
  desToForm     : DesToForm,
  fhddsConnector: FhddsConnector
)(implicit save4LaterService: Save4LaterService) extends AppController(ds, messagesApi) {

  val emailSessionKey = "declaration_email"
  val submitTimeKey = "submit_time"

  def showDeclaration() = SummaryAction(save4LaterService) { implicit request ⇒
    Ok(declaration(declarationForm, request.email, request.bpr))
  }

  def showAcknowledgment() = UserAction { implicit request ⇒
    val email: String = request.session.get(emailSessionKey).getOrElse("")

    val submitTime: String = request.session.get(submitTimeKey).getOrElse("Error, can not get the submit time for the form")
    Ok(
      acknowledgement_page(email, submitTime)
    )
  }

  def submitForm() = SummaryAction(save4LaterService).async { implicit request ⇒
    declarationForm.bindFromRequest().fold(
      formWithErrors => Future successful BadRequest(declaration(formWithErrors, request.email, request.bpr)),
      declaration => {
        sendSubscription(declaration) map { _ ⇒
          Redirect(routes.DeclarationController.showAcknowledgment()).withSession(request.session + (EmailSessionKey → declaration.email))
        }
      }
    )
  }

  private def sendSubscription(declaration: Declaration)(implicit request: SummaryRequest[_]) = {
    val subscription = getSubscriptionForDes(declaration)

    val submissionRequest =
      if (request.userIsRegistered) {
        amendedSubmissionRequest(declaration, subscription)
      } else {
       newSubmissionRequest(declaration, subscription)
      }

    val result = submissionRequest flatMap { fhddsConnector.submit(_) }
    result map { _ ⇒
      //remove data but we throw away the result of this operation
      save4LaterService.removeUserData(request.userId)
    }

    result
  }

  def newSubmissionRequest(declaration: Declaration, subscription: Subscription)(implicit request: SummaryRequest[_]): Future[SubmissionRequest] = {
    val payload = SubScriptionCreate(subscription)
    Future successful SubmissionRequest(
      request.bpr.safeId.get,
      declaration.email,
      Json toJson payload
    )
  }

  def amendedSubmissionRequest(declaration: Declaration, subscription: Subscription)(implicit request: SummaryRequest[_]): Future[SubmissionRequest] = {
    fhddsConnector.getSubmission(request.registrationNumber.get).map { response ⇒
      val prevSubscription = Subscription of response.subScriptionDisplay
      val changeIndicators = Diff.changeIndicators(prevSubscription, subscription)
      val payload = SubScriptionCreate.subscriptionAmend(changeIndicators, subscription)
      SubmissionRequest(
        request.registrationNumber.get,
        declaration.email,
        Json toJson payload
      )
    }
  }


  private def getSubscriptionForDes(d: Declaration)(implicit request: SummaryRequest[_]) = {
    request.businessType match {
      case BusinessType.CorporateBody ⇒ formToDes limitedCompanySubmission(request.bpr, Journeys ltdApplication request, d)
      case BusinessType.SoleTrader    ⇒ formToDes soleProprietorCompanySubmission(request.bpr, Journeys soleTraderApplication request, d)
      case BusinessType.Partnership   ⇒ formToDes partnership(request.bpr, Journeys partnershipApplication request, d)
    }
  }

}
