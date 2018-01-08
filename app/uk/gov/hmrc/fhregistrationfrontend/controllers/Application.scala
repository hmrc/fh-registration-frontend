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
import uk.gov.hmrc.auth.core.{NoActiveSession, _}
import uk.gov.hmrc.auth.otac.OtacFailureThrowable
import uk.gov.hmrc.fhregistrationfrontend.config.{ConcreteOtacAuthConnector, FrontendAuthConnector}
import uk.gov.hmrc.fhregistrationfrontend.connectors.{BusinessCustomerFrontendConnector, DFSUrls, FhddsConnector}
import uk.gov.hmrc.fhregistrationfrontend.connectors.ExternalUrls._
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.views.html.error_template_Scope0.error_template
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class Application @Inject()(
  links: ExternalUrls,
  ds: CommonPlayDependencies,
  fhddsConnector: FhddsConnector,
  messagesApi: play.api.i18n.MessagesApi,
  configuration: Configuration
) extends AppController(ds, messagesApi) {

  val businessCustomerConnector = BusinessCustomerFrontendConnector

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

  def continue = authorisedUser { implicit request ⇒ internalId ⇒
      businessCustomerConnector
        .getReviewDetails
        .map(details ⇒ {
          fhddsConnector.saveBusinessRegistrationDetails(internalId, formTypeRef(details), details)
          if (details.businessType.contains("Sole Trader")) Redirect(DFSUrls.dfsURL("Individual"))
          else Redirect(DFSUrls.dfsURL("Organisation"))
        })
  }

  private def formTypeRef(details: BusinessRegistrationDetails) = {
    DFSUrls.DFServiceLimitedCompanyFormName
  }

  private def getUserId(enrolments: Enrolments) = {
    enrolments.getEnrolment("IR-CT").flatMap(_.getIdentifier("UTR")).map(_.value).get
  }

  override def usewhiteListing = configuration.getBoolean("services.whitelisting.enabled").getOrElse(false)
}

@Singleton
abstract class AppController(ds: CommonPlayDependencies, messages: play.api.i18n.MessagesApi)
  extends FrontendController
    with I18nSupport
    with AuthorisedFunctions
    with Whitelisting
{

  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

  lazy val conf: Configuration = ds.conf
  implicit lazy val messagesApi: MessagesApi = ds.messagesApi

  override def authConnector: PlayAuthConnector = FrontendAuthConnector

  override def otacAuthConnector = ConcreteOtacAuthConnector
  lazy val authProvider: AuthProviders = AuthProviders(GovernmentGateway)
  val hasCtUtr: Predicate = Enrolment("IR-CT")

  def ggAuthorised(action: Request[AnyContent] ⇒ Future[Result]): Action[AnyContent] = {
    Action.async { implicit request ⇒
      println(s"=====${request.session}")
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
      case x: NoActiveSession ⇒
        Logger.warn(s"could not authenticate user due to: No Active Session " + x)

        val ggRedirectParms = Map(
          "continue" -> Seq(continueUrl),
          "origin" -> Seq(getString("appName"))
        )

        Redirect(ggLoginUrl, ggRedirectParms)
      case e: OtacFailureThrowable ⇒
        val errorTemplate = new error_template()
        Unauthorized(errorTemplate.render("Unauthorized", "Unauthorized", "You are not authorized to use this service", request, messages))
      case ex ⇒
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
