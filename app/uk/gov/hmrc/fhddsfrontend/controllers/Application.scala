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

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.api.{Configuration, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.fhddsfrontend.config.FrontendAuthConnector
import uk.gov.hmrc.fhddsfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhddsfrontend.models._
import uk.gov.hmrc.fhddsfrontend.models.FHDDSExternalUrls._
import uk.gov.hmrc.fhddsfrontend.views.html.address_inf
import uk.gov.hmrc.fhddsfrontend.views.html.start_page
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class Application @Inject()(ds: CommonPlayDependencies, fhddsConnector: FhddsConnector) extends AppController(ds) {

  def start(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(start_page()))
  }

  def information(formName: String): Action[AnyContent] = authorisedUser {
    implicit request ⇒
      implicit userEnrolments ⇒
        val affinityGroup = request.session.get("affinityGroup").getOrElse("")
        if (affinityGroup == formName) {
          fhddsConnector.lookupCompanyDetails().map {
            case CompanyDetails(address, org) ⇒ Ok(address_inf(formName,
                                                   Forms.confirmForm,
                                                   address.getOrElse(Address("")),
                                                   org.getOrElse(Company(title = "")).title))
          }
        }
        else if (affinityGroup == "Agent") Future.successful(BadRequest("Not support agents yet"))
        else Future.successful(BadRequest("Affinity group not match the form"))
  }

  def showForm(formName: String): Action[AnyContent] = authorisedUser {
    implicit request ⇒
      implicit userEnrolments ⇒
        Forms.confirmForm.bindFromRequest().fold(
          formWithErrors => {
            fhddsConnector.lookupCompanyDetails().map {
              case CompanyDetails(address, org) ⇒ Ok(address_inf(formName,
                                                                 formWithErrors,
                                                                 address.getOrElse(Address("")),
                                                                 org.getOrElse(Company(title = "")).title))
            }
          },
          apply => {
            if (apply.value) Future.successful(Redirect(DFSURL.dfsURL(formName)))
            else Future.successful(BadRequest("please update your address with company house"))
          }
        )
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

  def authorisedUser(action: Request[AnyContent] ⇒ Enrolments ⇒ Future[Result]): Action[AnyContent] = {
    Action.async { implicit request ⇒
      authorised(authProvider).retrieve(allEnrolments) {
        userEnrolments ⇒ {
          action(request)(userEnrolments)
        }
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
final class CommonPlayDependencies @Inject()(val conf: Configuration, val messagesApi: MessagesApi)

case class UnexpectedState(errorMsg: String, json: Option[JsValue] = None)

object UnexpectedState {
  implicit val invalidStateFormat: OFormat[UnexpectedState] = Json.format[UnexpectedState]
}
