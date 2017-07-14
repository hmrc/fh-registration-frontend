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

import cats.data.EitherT
import cats.instances.future._
import cats.syntax.either._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.api.{Configuration, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.fhddsfrontend.config.FrontendAuthConnector
import uk.gov.hmrc.fhddsfrontend.connectors.DESConnectorImpl
import uk.gov.hmrc.fhddsfrontend.models.FHDDSExternalUrls._
import uk.gov.hmrc.fhddsfrontend.models.{EtmpAddress, FindBusinessDataResponse, OrganisationResponse, Utr}
import uk.gov.hmrc.fhddsfrontend.views.html.start_page
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class Application @Inject()(ds: CommonPlayDependencies, DESConnector:DESConnectorImpl) extends AppController(ds, DESConnector) {

  def start(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(start_page()))
  }

}

@Singleton
abstract class AppController(ds: CommonPlayDependencies, DESConnector:DESConnectorImpl)
  extends FrontendController with I18nSupport with AuthorisedFunctions {

  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

  lazy val conf: Configuration = ds.conf
  implicit lazy val messagesApi: MessagesApi = ds.messagesApi

  override def authConnector: uk.gov.hmrc.auth.core.AuthConnector = FrontendAuthConnector

  lazy val authProvider: AuthProviders = AuthProviders(GovernmentGateway)

  def authorisedUser(action: Request[AnyContent] ⇒ EtmpAddress ⇒ OrganisationResponse ⇒ Future[Result]): Action[AnyContent] = {
    Action.async { implicit request ⇒
      authorised(authProvider).retrieve(allEnrolments) {
        userDetailsUri ⇒ {
          val utr = for {
            enrolment ← userDetailsUri.getEnrolment("IR-CT")
            utr ← enrolment.getIdentifier("UTR")
          } yield Utr(utr.value)

          val organisationDetails = DESConnector.lookup(utr.get)

          val response = EitherT(organisationDetails.map { response =>
            Json.parse(response.body).validate[FindBusinessDataResponse].asEither.leftMap(_ => UnexpectedState(response.body))
          }).toOption.subflatMap { findResponse => Some(findResponse) }

          val address = response.map(find ⇒ find.address).value.map(a ⇒ a.get)
          val organisation = response.map(find ⇒ find.organisation).value.map(o ⇒ o.get).map(o ⇒ o.get)

          organisation.flatMap(o ⇒ address.flatMap(a ⇒ action(request)(a)(o)))
        }
      } recover { case e ⇒ handleFailure(e) }
    }
  }

  def handleFailure(e: Throwable): Result =
    e match {
      case x: NoActiveSession ⇒
        Logger.warn(s"could not authenticate user due to: No Active Session " + x)
        Redirect(ggLoginUrl, Map(
          "continue" -> Seq(continueUrl),
          "origin" -> Seq(continueUrl)
        ))
      case ex ⇒
        Logger.warn(s"could not authenticate user due to: $ex")
        Redirect(routes.Application.start())
    }
}

@Singleton
final class CommonPlayDependencies @Inject()(val conf: Configuration, val messagesApi: MessagesApi)

case class UnexpectedState(errorMsg: String, json: Option[JsValue] = None)

object UnexpectedState {
  implicit val invalidStateFormat: OFormat[UnexpectedState] = Json.format[UnexpectedState]
}