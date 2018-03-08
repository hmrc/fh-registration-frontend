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


import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.fhregistrationfrontend.actions.{EnrolledUserAction, ResumeJourneyAction, UserAction}
import uk.gov.hmrc.fhregistrationfrontend.config.{ConcreteOtacAuthConnector, FrontendAuthConnector}
import uk.gov.hmrc.fhregistrationfrontend.connectors.ExternalUrls._
import uk.gov.hmrc.fhregistrationfrontend.connectors._
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessTypeForm.businessTypeForm
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.html.{business_type, _}
import uk.gov.hmrc.fhregistrationfrontend.views.html.forms._
import uk.gov.hmrc.fhregistrationfrontend.views.html.registrationstatus._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class Application @Inject()(
  links            : ExternalUrls,
  ds               : CommonPlayDependencies,
  fhddsConnector   : FhddsConnector,
  save4LaterService: Save4LaterService
) extends AppController(ds) {


  override def usewhiteListing = configuration.getBoolean("services.whitelisting.enabled").getOrElse(false)

  override val configuration: Configuration = ds.conf

  val businessCustomerConnector = BusinessCustomerFrontendConnector

  val formMaxExpiryDays: Int = configuration.getInt(s"formMaxExpiryDays").getOrElse(27)

  def whitelisted(p: String) = Action.async {
    implicit request ⇒
      authorised() {
        val verificationUrl = configuration.getString("services.verificationUrl").getOrElse("http://localhost:9227/verification/otac/login")
        Future successful Redirect(s"$verificationUrl?p=$p").withSession(request.session + (SessionKeys.redirect → routes.Application.start().url))
      } recover {
        case x: NoActiveSession ⇒
          Logger.warn(s"could not authenticate user due to: No Active Session " + x)

          val ggRedirectParms = Map(
            "continue" -> Seq(s"$continueUrl/whitelisted?p=$p"),
            "origin" -> Seq(getString("appName"))
          )
          Redirect(ggLoginUrl, ggRedirectParms)
      }
  }

  def start = UserAction { implicit request ⇒
    request.registrationNumber match {
      case Some(_) ⇒ Redirect(routes.Application.checkStatus())
      case None ⇒ Redirect(links.businessCustomerVerificationUrl)
    }
  }

  def continue = UserAction.async { implicit request ⇒
    for {
      details ← businessCustomerConnector.getReviewDetails
      _ ← save4LaterService.saveBusinessRegistrationDetails(request.userId, details)
      businessType ← save4LaterService.fetchBusinessType(request.userId)
      hasBusinessType = businessType.isEmpty
    } yield {
      Redirect(routes.Application.deleteOrContinue(hasBusinessType))
    }
  }

  val deleteOrContinueForm = Form("deleteOrContinue" → nonEmptyText)

  def deleteOrContinue(isNewForm: Boolean) = UserAction.async { implicit request ⇒
    if (isNewForm) {
      Future successful Redirect(routes.Application.businessType())
    }
    else {
      save4LaterService.fetchLastUpdateTime(request.userId) map {
        case Some(savedDate) ⇒
          Ok(continue_delete(new DateTime(savedDate), deleteOrContinueForm))
        case None            ⇒ Redirect(routes.Application.businessType())
      }
    }
  }

  def submitDeleteOrContinue = UserAction.async { implicit request ⇒
    deleteOrContinueForm.bindFromRequest().fold(
      formWithErrors => Future successful errorResultsPages(Results.ServiceUnavailable),
      deleteOrContinue => {
        if (deleteOrContinue == "delete") {
          save4LaterService.fetchLastUpdateTime(request.userId) flatMap {
            case Some(savedDate) ⇒ Future successful Ok(confirm_delete(new DateTime(savedDate)))
            case None            ⇒
              Future successful errorResultsPages(Results.ServiceUnavailable)
          }
        } else {
          Future successful Redirect(routes.Application.resumeForm())
        }
      }
    )
  }

  def confirmDelete = UserAction.async { implicit request ⇒
      save4LaterService.fetchLastUpdateTime(request.userId) flatMap {
        case Some(savedDate) ⇒ Future successful Ok(confirm_delete(new DateTime(savedDate)))
        case None            ⇒ Future successful ServiceUnavailable
      }
  }

  def resumeForm = ResumeJourneyAction()(save4LaterService, messages) { implicit request ⇒
    if(request.journeyState.isComplete)
      Redirect(routes.SummaryController.summary())
    else {
      request.journeyState.lastEditedPage.map( p ⇒ p.id → p.lastSection) match {
        case None ⇒ Redirect(routes.Application.startForm())
        case Some((pid , Some(section))) ⇒  Redirect(routes.FormPageController.loadWithSection(pid, section))
        case Some((pid, None)) ⇒ Redirect(routes.FormPageController.load(pid))

      }
    }
  }

  def deleteUserData = UserAction.async { implicit request ⇒
    save4LaterService.removeUserData(request.userId) map {
      _ ⇒ Redirect(routes.Application.start())
    }
  }

  def businessType = UserAction { implicit request ⇒
    Ok(business_type(businessTypeForm))
  }

  def submitBusinessType = UserAction.async { implicit request ⇒
    businessTypeForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(business_type(formWithErrors))),
      businessType => {
        for {
          _ ← save4LaterService.saveBusinessType(request.userId, businessType)
        } yield {
          Redirect(routes.Application.startForm())
        }
      }
    )
  }

  def startForm = UserAction.async { implicit request ⇒
    save4LaterService.fetchBusinessRegistrationDetails(request.userId) map {
      case Some(bpr) ⇒ Redirect(routes.FormPageController.load("mainBusinessAddress"))
      case None      ⇒ Redirect(links.businessCustomerVerificationUrl)
    }
  }

  def savedForLater = UserAction.async { implicit request ⇒
    save4LaterService.fetchLastUpdateTime(request.userId).map {
      case Some(savedDate) ⇒ Ok(saved(new DateTime(savedDate).plusDays(formMaxExpiryDays)))
      case None            ⇒ errorResultsPages(Results.NotFound)
    }
  }

  def checkStatus() = EnrolledUserAction().async { implicit request ⇒
    def canAmend: Boolean = false
    fhddsConnector
      .getStatus(request.registrationNumber)(hc)
      .map(statusResp ⇒ {
        Ok(status(statusResp.body, request.registrationNumber, canAmend))
      })
  }

  def componentExamples = Action.async { implicit request =>
    Future(Ok(examples()))
  }

}

@Singleton
abstract class AppController(ds: CommonPlayDependencies)
  extends FrontendController
    with I18nSupport
    with AuthorisedFunctions
    with Whitelisting
    with UnexpectedState {

  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

  def usewhiteListing: Boolean = false

  override implicit val messagesApi: MessagesApi = ds.messagesApi

  override def authConnector: PlayAuthConnector = FrontendAuthConnector

  override def otacAuthConnector = ConcreteOtacAuthConnector

  val configuration: Configuration = ds.conf
  val messages: MessagesApi = messagesApi

  lazy val authProvider: AuthProviders = AuthProviders(GovernmentGateway)
  val hasCtUtr: Predicate = Enrolment("IR-CT")

}

@Singleton
final class CommonPlayDependencies @Inject()(val conf: Configuration, val env: Environment, val messagesApi: MessagesApi)

trait UnexpectedState {
  import Results._

  def errorResultsPages(errorResults: Status, errorMsg: Option[String] = None)(implicit request: Request[_], messages: Messages): Result = {

    errorResults match {
      case NotFound ⇒ NotFound(error_template(
        messages("fh.generic.not_found"),
        messages("fh.generic.not_found.label"),
        errorMsg.getOrElse(messages("fh.generic.not_found.inf"))
      ))
      case BadRequest ⇒ BadRequest(error_template(
        messages("fh.generic.bad_request"),
        messages("fh.generic.bad_request.label"),
        errorMsg.getOrElse(messages("fh.generic.bad_request.inf"))
      ))
      case Unauthorized ⇒ Unauthorized(error_template(
        messages("fh.generic.unauthorized"),
        messages("fh.generic.unauthorized.label"),
        errorMsg.getOrElse(messages("fh.generic.unauthorized.inf"))
      ))
      case BadGateway ⇒ BadGateway(error_template(
        messages("fh.generic.bad_gateway"),
        messages("fh.generic.bad_gateway.label"),
        errorMsg.getOrElse(messages("fh.generic.bad_gateway.inf"))
      ))
      case _ ⇒ InternalServerError(error_template(
        messages("fh.generic.internal_server_error"),
        messages("fh.generic.internal_server_error.label"),
        errorMsg.getOrElse(messages("fh.generic.internal_server_error.inf"))
      ))
    }

  }
}
