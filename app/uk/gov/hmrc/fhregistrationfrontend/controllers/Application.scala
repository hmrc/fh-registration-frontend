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

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json._
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrievals.internalId
import uk.gov.hmrc.auth.otac.OtacFailureThrowable
import uk.gov.hmrc.fhregistrationfrontend.actions.{EnrolledUserAction, SummaryAction, UserAction}
import uk.gov.hmrc.fhregistrationfrontend.config.{ConcreteOtacAuthConnector, FrontendAuthConnector}
import uk.gov.hmrc.fhregistrationfrontend.connectors.ExternalUrls._
import uk.gov.hmrc.fhregistrationfrontend.connectors._
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessTypeForm.businessTypeForm
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.DeclarationForm.declarationForm
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.services.{Save4LaterKeys, Save4LaterService}
import uk.gov.hmrc.fhregistrationfrontend.views.html.forms._
import uk.gov.hmrc.fhregistrationfrontend.views.html.registrationstatus._
import uk.gov.hmrc.fhregistrationfrontend.views.html._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class Application @Inject()(
  links            : ExternalUrls,
  ds               : CommonPlayDependencies,
  fhddsConnector   : FhddsConnector,
  messagesApi      : play.api.i18n.MessagesApi,
  configuration    : Configuration,
  save4LaterService: Save4LaterService
) extends AppController(ds, messagesApi) {

  val businessCustomerConnector = BusinessCustomerFrontendConnector

  val soleTraderFormTypeRef: String = configuration.getString(s"fhdds-dfs-frontend.fhdds-sole-proprietor").getOrElse("fhdds-sole-proprietor")
  val limitedCompanyFormTypeRef: String = configuration.getString(s"fhdds-dfs-frontend.fhdds-limited-company").getOrElse("fhdds-limited-company")
  val partnershipFormTypeRef: String = configuration.getString(s"fhdds-dfs-frontend.fhdds-partnership").getOrElse("fhdds-partnership")

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
          Ok(continue_delete(savedDate, deleteOrContinueForm))
        case None            ⇒ Redirect(routes.Application.businessType())
      }
    }
  }

  def submitDeleteOrContinue = UserAction.async { implicit request ⇒
    deleteOrContinueForm.bindFromRequest().fold(
      formWithErrors => Future successful ServiceUnavailable,
      deleteOrContinue => {
        if (deleteOrContinue == "delete") {
          save4LaterService.fetchLastUpdateTime(request.userId) flatMap {
            case Some(savedDate) ⇒ Future successful Ok(confirm_delete(savedDate))
            case None            ⇒ Future successful ServiceUnavailable
          }
        } else {
          //todo goto the right page
          Future successful Redirect(routes.Application.startForm())
        }
      }
    )
  }

  def deleteUserData = UserAction.async { implicit request ⇒
    save4LaterService.removeUserData(request.userId) map {
      _ ⇒ Redirect(routes.Application.start())
    }
  }

  // todo placeholder
  def confirmDeleteSection(sectionName: String) = UserAction.async { implicit request ⇒
    Future successful Ok(confirm_delete_section("test", sectionName))
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
      case Some(savedDate) ⇒ Ok(saved(savedDate.plusDays(formMaxExpiryDays)))
      case None            ⇒ errorResultsPages(NotFound)
    }
  }

  def summary = UserAction.async  { implicit request ⇒
    save4LaterService.fetchBusinessRegistrationDetails(request.userId) map {
      case Some(bpr) ⇒ Ok(ltd_summary(TestData.mockSummary, bpr))
      case None      ⇒ Redirect(links.businessCustomerVerificationUrl)
    }

  }

  def checkStatus() = EnrolledUserAction().async { implicit request ⇒
    fhddsConnector
      .getStatus(request.registrationNumber)(hc)
      .map(statusResp ⇒ {
        Ok(status(statusResp.body, request.registrationNumber))
      })
  }

  def componentExamples = Action.async { implicit request =>
    Future(Ok(examples()))
  }

  override def usewhiteListing = configuration.getBoolean("services.whitelisting.enabled").getOrElse(false)
}

@Singleton
abstract class AppController(ds: CommonPlayDependencies, messages: play.api.i18n.MessagesApi)
  extends FrontendController
    with I18nSupport
    with AuthorisedFunctions
    with Whitelisting {

  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

  override def usewhiteListing: Boolean = false

  lazy val conf: Configuration = ds.conf
  implicit lazy val messagesApi: MessagesApi = ds.messagesApi

  override def authConnector: PlayAuthConnector = FrontendAuthConnector

  override def otacAuthConnector = ConcreteOtacAuthConnector

  lazy val authProvider: AuthProviders = AuthProviders(GovernmentGateway)
  val hasCtUtr: Predicate = Enrolment("IR-CT")

  def ggAuthorised(action: Request[AnyContent] ⇒ Future[Result]): Action[AnyContent] = {
    Action.async { implicit request ⇒
      withVerifiedPasscode("fhdds", request.session.get(SessionKeys.otacToken)) {
        authorised() {
          action(request)
        }
      } recover { case e ⇒ handleFailure(e) }
    }
  }

  def authorisedUser(action: Request[AnyContent] ⇒ String ⇒ Future[Result]): Action[AnyContent] = {
    Action.async { implicit request ⇒
      withVerifiedPasscode("fhdds", request.session.get(SessionKeys.otacToken)) {
        authorised().retrieve(internalId) {
          case Some(iid) ⇒ action(request)(iid)
          case None      ⇒ throw AuthorisationException.fromString("Can not find user id")
        } recover { case e ⇒ handleFailure(e) }
      }
    }
  }

  def handleFailure(e: Throwable)(implicit request: Request[_], messages: Messages): Result =
    e match {
      case x: NoActiveSession      ⇒
        Logger.warn(s"could not authenticate user due to: No Active Session " + x)
        val ggRedirectParms = Map(
          "continue" -> Seq(continueUrl),
          "origin" -> Seq(getString("appName"))
        )
        Redirect(ggLoginUrl, ggRedirectParms)
      case e: OtacFailureThrowable ⇒
        errorResultsPages(Unauthorized)
      case ex                      ⇒
        Logger.warn(s"could not authenticate user due to: $ex")
        errorResultsPages(BadRequest, Some(s"$ex"))
    }

  def errorResultsPages(errorResults: Status, inf: Option[String] = None)(implicit request: Request[_]): Result = {
    errorResults match {
      case NotFound ⇒ NotFound(error_template(
        messages("fh.generic.not_found"),
        messages("fh.generic.not_found.label"),
        inf.getOrElse(messages("fh.generic.not_found.inf"))
      ))
      case BadRequest ⇒ BadRequest(error_template(
        messages("fh.generic.bad_request"),
        messages("fh.generic.bad_request.label"),
        inf.getOrElse(messages("fh.generic.bad_request.inf"))
      ))
      case Unauthorized ⇒ Unauthorized(error_template(
        messages("fh.generic.unauthorized"),
        messages("fh.generic.unauthorized.label"),
        inf.getOrElse(messages("fh.generic.unauthorized.inf"))
      ))
      case BadGateway ⇒ BadGateway(error_template(
        messages("fh.generic.bad_gateway"),
        messages("fh.generic.bad_gateway.label"),
        inf.getOrElse(messages("fh.generic.bad_gateway.inf"))
      ))
      case _ ⇒ InternalServerError(error_template(
        messages("fh.generic.internal_server_error"),
        messages("fh.generic.internal_server_error.label"),
        inf.getOrElse(messages("fh.generic.internal_server_error.inf"))
      ))
    }

  }

}

@Singleton
final class CommonPlayDependencies @Inject()(val conf: Configuration, val env: Environment, val messagesApi: MessagesApi)

case class UnexpectedState(errorMsg: String, json: Option[JsValue] = None)

object UnexpectedState {
  implicit val invalidStateFormat: OFormat[UnexpectedState] = Json.format[UnexpectedState]
}
