/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.fhregistrationfrontend.config.{AppConfig, FrontendAppConfig}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.views.html.{show_all_submissions, temp_admin_page}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController



@Singleton
class AdminPageController @Inject() (frontendAppConfig: FrontendAppConfig, val messagesApi: MessagesApi, appConfig: AppConfig, fhddsConnector: FhddsConnector) extends FrontendController with I18nSupport {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  lazy val credentials = Credentials(frontendAppConfig.username, frontendAppConfig.password)

  def showAdminPage: Action[AnyContent] = AuthenticationController(credentials) { implicit request =>

    Ok(temp_admin_page(request, frontendAppConfig, messagesApi))
  }

  def getSubmissions: Action[AnyContent] = AuthenticationController(credentials).async { implicit request =>

    fhddsConnector.getAllSubmission().map {
      case submissions if submissions.nonEmpty => Ok(show_all_submissions(submissions)(request,frontendAppConfig,messagesApi))
      case submissions if submissions.isEmpty => Ok("No Submissions found")
    }
  }

  def getUserInfo(userId: String) = AuthenticationController(credentials).async { implicit request =>
    fhddsConnector.getUserInfo(userId).map {
      response => Ok(response.json)
    }
  }

  def getGroupInfo(groupId: String) = AuthenticationController(credentials).async { implicit request =>
    fhddsConnector.getGroupInfo(groupId).map {
      response => Ok(response.json)
    }
  }
}
