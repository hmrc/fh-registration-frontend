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

import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrievals.internalId
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.otac.OtacFailureThrowable
import uk.gov.hmrc.fhregistrationfrontend.config.{ConcreteOtacAuthConnector, FrontendAuthConnector}
import uk.gov.hmrc.fhregistrationfrontend.connectors.ExternalUrls._
import uk.gov.hmrc.fhregistrationfrontend.connectors._
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessTypeForm.businessTypeForm
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.html.error_template_Scope0.error_template
import uk.gov.hmrc.fhregistrationfrontend.views.html.forms._
import uk.gov.hmrc.fhregistrationfrontend.views.html.ltd_summary
import uk.gov.hmrc.fhregistrationfrontend.views.html.business_type
import uk.gov.hmrc.fhregistrationfrontend.views.html.saved
import uk.gov.hmrc.fhregistrationfrontend.views.html.registrationstatus._
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

  def start = ggAuthorised { implicit request ⇒
    Future.successful(Redirect(links.businessCustomerVerificationUrl))
  }

  def continue = authorisedUser { implicit request ⇒
    internalId ⇒
      for {
        details ← businessCustomerConnector.getReviewDetails
        _ ← save4LaterService.saveBusinessRegistrationDetails(internalId, details)
      } yield {
        Redirect(routes.Application.businessType())
      }
  }

  def businessType = authorisedUser { implicit request ⇒
    internalId ⇒
      Future.successful(Ok(business_type(businessTypeForm)))
  }

  def submitBusinessType = authorisedUser { implicit request ⇒
    internalId ⇒
      businessTypeForm.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(business_type(formWithErrors))),
        businessType => {
          for {
            _ ← save4LaterService.saveBusinessType(internalId, businessType.businessType)
          } yield {
            Redirect(routes.Application.startForm())
          }
        }
      )
  }

  def startForm = authorisedUser { implicit request ⇒
    internalId ⇒
      save4LaterService.fetchBusinessRegistrationDetails(internalId) map {
        case Some(bpr) ⇒ Redirect(routes.FormPageController.load("mainBusinessAddress"))
        case None      ⇒ Redirect(links.businessCustomerVerificationUrl)
      }
  }

  def savedForLater = authorisedUser { implicit request ⇒
    internalId ⇒
      Future.successful(Ok(saved("Saturday 17 March 2018")))
  }

  def summary = Action.async { implicit request ⇒
    Future.successful(Ok(ltd_summary("ok")))
  }

  def checkStatus(fhddsRegistrationNumber: String) = Action.async { implicit request ⇒
    fhddsConnector
      .getStatus(fhddsRegistrationNumber: String)(hc)
      .map(statusResp ⇒ {
        Ok(status(statusResp.body, fhddsRegistrationNumber))
      })
  }

  def componentExamples = Action.async { implicit request =>
    Future(Ok(examples()))
  }


  private def formTypeRef(details: BusinessRegistrationDetails) = {

    details.businessType match {
      case Some("Sole Trader")    ⇒ soleTraderFormTypeRef
      case Some("corporate body") ⇒ limitedCompanyFormTypeRef
      case Some("Partnership")    ⇒ partnershipFormTypeRef
      case _                      ⇒ limitedCompanyFormTypeRef
    }
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
          case Some(iid) ⇒ {
            action(request)(iid)
          }
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
        val errorTemplate = new error_template()
        Unauthorized(errorTemplate.render("Unauthorized", "Unauthorized", "You are not authorized to use this service", request, messages))
      case ex                      ⇒
        Logger.warn(s"could not authenticate user due to: $ex")
        BadRequest(s"$ex")
    }
}

@Singleton
final class CommonPlayDependencies @Inject()(val conf: Configuration, val env: Environment, val messagesApi: MessagesApi)

case class UnexpectedState(errorMsg: String, json: Option[JsValue] = None)

object UnexpectedState {
  implicit val invalidStateFormat: OFormat[UnexpectedState] = Json.format[UnexpectedState]
}
