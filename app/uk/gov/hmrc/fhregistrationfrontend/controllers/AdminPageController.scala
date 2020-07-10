/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents}
import uk.gov.hmrc.fhregistrationfrontend.config.{AppConfig, FrontendAppConfig}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.controllers.AdminRequest.requestForm
import uk.gov.hmrc.fhregistrationfrontend.controllers.EnrolmentForm.{allocateEnrolmentForm, deleteEnrolmentForm}
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.views.html._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminPageController @Inject()(
  frontendAppConfig: FrontendAppConfig,
  implicit val appConfig: AppConfig,
  fhddsConnector: FhddsConnector,
  cc: MessagesControllerComponents,
  views: Views
)(implicit ec: ExecutionContext, config: Configuration)
    extends FrontendController(cc) with play.api.i18n.I18nSupport {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  lazy val credentials = Credentials(frontendAppConfig.username, frontendAppConfig.password)
  val bodyParser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
  val authAction = AuthenticationController(credentials, bodyParser)

  def showAdminPage: Action[AnyContent] = authAction { implicit request =>
    Ok(views.temp_admin_page())
  }

  def getSubmissions: Action[AnyContent] = authAction.async { implicit request =>
    fhddsConnector.getAllSubmission().map {
      case submissions if submissions.nonEmpty => Ok(views.show_all_submissions(submissions))
      case submissions if submissions.isEmpty  => Ok("No Submissions found")
    }
  }

  def loadDeletePage(formBundleId: String) = authAction.async { implicit request =>
    fhddsConnector.getSubMission(formBundleId).map {
      case submission => Ok(views.show_submission(submission))
      case _          => Ok(s"No Submission found for $formBundleId")
    }
  }

  def deleteSubmission(formBundleId: String): Action[AnyContent] = authAction.async { implicit request =>
    fhddsConnector
      .deleteSubmission(formBundleId)
      .map(_ => Ok(s"Submission data for $formBundleId has been deleted "))
      .recover { case _ => Ok(s"Submission with $formBundleId not found") }

  }

  def loadUserIdPage = authAction { implicit request =>
    Ok(views.admin_get_groupID(requestForm))
  }

  def sendAdminRequest() = authAction.async { implicit request =>
    requestForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.admin_get_groupID(formWithErrors)))
      },
      formData =>
        fhddsConnector
          .addEnrolment(formData.userId, formData.groupId, formData.registrationNumber)
          .map(result => Ok(result.body))
    )
  }

  def loadAllocateEnrolment = authAction { implicit request =>
    Ok(views.allocate_enrolment(allocateEnrolmentForm))
  }

  def allocateEnrolment = authAction.async { implicit request =>
    allocateEnrolmentForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.allocate_enrolment(formWithErrors)))
      },
      formData => {
        fhddsConnector.allocateEnrolment(formData.userId, formData.registrationNumber).map(result => Ok(result.body))
      }
    )
  }

  def loadDeleteEnrolment = authAction { implicit request =>
    Ok(views.delete_enrolment(deleteEnrolmentForm))
  }

  def deleteEnrolment = authAction.async { implicit request =>
    deleteEnrolmentForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.delete_enrolment(formWithErrors)))

      },
      formData => {
        fhddsConnector.deleteEnrolment(formData.userId, formData.registrationNumber).map(result => Ok(result.body))
      }
    )
  }

  def checkStatus(regNo: String) = authAction.async { implicit request =>
    fhddsConnector
      .getStatus(regNo)(hc)
      .map(result => Ok(result.toString))
  }

  def getUserInfo(userId: String) = authAction.async { implicit request =>
    fhddsConnector.getUserInfo(userId).map { response =>
      Ok(response.json)
    }
  }

  def getGroupInfo(groupId: String) = authAction.async { implicit request =>
    fhddsConnector.getGroupInfo(groupId).map { response =>
      Ok(response.json)
    }
  }

  def es2(userId: String) = authAction.async { implicit request =>
    fhddsConnector.es2Info(userId).map { response =>
      Ok(response.json)
    }
  }

  def es3(groupId: String) = authAction.async { implicit request =>
    fhddsConnector.es3Info(groupId).map { response =>
      Ok(response.json)
    }
  }
}

case class AdminRequest(userId: String, groupId: String, registrationNumber: String)

object AdminRequest {
  val format: Format[AdminRequest] = Json.format[AdminRequest]

  val requestForm: Form[AdminRequest] = Form(
    mapping(
      "userId"             -> nonEmptyText,
      "groupId"            -> nonEmptyText,
      "registrationNumber" -> nonEmptyText
    )(AdminRequest.apply)(AdminRequest.unapply))
}

case class EnrolmentForm(userId: String, registrationNumber: String)

object EnrolmentForm {

  val format: Format[EnrolmentForm] = Json.format[EnrolmentForm]

  val allocateEnrolmentForm: Form[EnrolmentForm] = Form(
    mapping(
      "userId"             -> nonEmptyText,
      "registrationNumber" -> nonEmptyText
    )(EnrolmentForm.apply)(EnrolmentForm.unapply))

  val deleteEnrolmentForm: Form[EnrolmentForm] = allocateEnrolmentForm
}
