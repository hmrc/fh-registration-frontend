/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.fhddsfrontend.controllers


import javax.inject.{Inject, Singleton}

import play.api.Environment
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.Retrievals.{allEnrolments, externalId, internalId}
import uk.gov.hmrc.auth.core.{NoActiveSession, _}
import uk.gov.hmrc.fhddsfrontend.config.FrontendAuthConnector
import uk.gov.hmrc.fhddsfrontend.connectors.{BusinessCustomerFrontendConnector, FhddsConnector}
import uk.gov.hmrc.fhddsfrontend.models.DFSURL
import uk.gov.hmrc.fhddsfrontend.models.FHDDSExternalUrls._
import uk.gov.hmrc.fhddsfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class Application @Inject()(
  links: ExternalUrls,
  ds: CommonPlayDependencies,
  fhddsConnector: FhddsConnector) extends AppController(ds) {

  val businessCustomerConnector = BusinessCustomerFrontendConnector

  def start = ggAuthorised { implicit request ⇒
    Future.successful(Redirect(links.businessCustomerVerificationUrl))
  }

  def continue = authorisedUser { implicit request ⇒ internalId ⇒
      businessCustomerConnector
        .getReviewDetails
        .flatMap(details ⇒ fhddsConnector.saveBusinessRegistrationDetails(internalId, formTypeRef(details), details))
        .map(_ ⇒ Redirect(DFSURL.dfsURL("Organisation")))
  }

  private def formTypeRef(details: BusinessRegistrationDetails) = {
    DFSURL.DFServiceLimitedCompanyFormName
  }

  private def getUserId(enrolments: Enrolments) = {
    enrolments.getEnrolment("IR-CT").flatMap(_.getIdentifier("UTR")).map(_.value).get
  }

}

@Singleton
abstract class AppController(ds: CommonPlayDependencies)
  extends FrontendController with I18nSupport with AuthorisedFunctions {

  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

  lazy val conf: Configuration = ds.conf
  implicit lazy val messagesApi: MessagesApi = ds.messagesApi

  override def authConnector: uk.gov.hmrc.auth.core.AuthConnector = FrontendAuthConnector

  lazy val authProvider: AuthProviders = AuthProviders(GovernmentGateway)
  val hasCtUtr: Predicate = Enrolment("IR-CT")

  def ggAuthorised(action: Request[AnyContent] ⇒ Future[Result]): Action[AnyContent] = {
    Action.async { implicit request ⇒
      authorised(authProvider) {
        action(request)
      } recover { case e ⇒ handleFailure(e) }
    }
  }


  def authorisedUser(action: Request[AnyContent] ⇒ String ⇒ Future[Result]): Action[AnyContent] = {
    Action.async { implicit request ⇒
      authorised(authProvider).retrieve(internalId ) {
        case Some(iid)⇒ {
          action(request)(iid)
        }
        case None ⇒ throw AuthorisationException.fromString("Can not find user id")
      } recover { case e ⇒ handleFailure(e) }
    }
  }

  def handleFailure(e: Throwable): Result =
    e match {
      case x: NoActiveSession ⇒
        Logger.warn(s"could not authenticate user due to: No Active Session " + x)

        val ggRedirectParms = Map(
          "continue" -> Seq(continueUrl),
          "origin" -> Seq(getString("appName"))
        )

        Redirect(ggLoginUrl, ggRedirectParms)
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
