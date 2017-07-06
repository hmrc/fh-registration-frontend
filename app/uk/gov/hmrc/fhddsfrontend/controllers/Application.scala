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

import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.fhddsfrontend.FrontendAuthConnector
import uk.gov.hmrc.fhddsfrontend.models.FHDDSRegime
import uk.gov.hmrc.fhddsfrontend.views.html.start_page
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class Application @Inject()(override val messagesApi: MessagesApi)
  extends FrontendController with I18nSupport {

  def start(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(start_page()))
  }
}

@Singleton
abstract class AppController(ds: CommonPlayDependencies)
  extends FrontendController with I18nSupport with Actions {

  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

  lazy val conf: Configuration = ds.conf
  implicit lazy val messagesApi: MessagesApi = ds.messagesApi
  override val authConnector: AuthConnector = FrontendAuthConnector

  def authorised: AuthenticatedBy = AuthorisedFor(taxRegime = FHDDSRegime, pageVisibility = GGConfidence)

}

@Singleton
final class CommonPlayDependencies @Inject()(val conf: Configuration, val messagesApi: MessagesApi)