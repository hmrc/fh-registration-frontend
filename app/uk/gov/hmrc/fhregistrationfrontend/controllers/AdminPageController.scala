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
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.fhregistrationfrontend.config.{AppConfig, FrontendAppConfig}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.views.html.{admin_get_groupID, allocate_enrolment, delete_enrolment, show_all_submissions, show_submission, temp_admin_page}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.fhregistrationfrontend.controllers.AdminRequest.requestForm
import uk.gov.hmrc.fhregistrationfrontend.controllers.EnrolmentForm.allocateEnrolmentForm
import uk.gov.hmrc.fhregistrationfrontend.controllers.EnrolmentForm.deleteEnrolmentForm
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.FhddsStatus
import uk.gov.hmrc.fhregistrationfrontend.views.registrationstatus.StatusPageParams

import scala.concurrent.Future

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

  def loadDeletePage(formBundleId:String)= AuthenticationController(credentials).async { implicit request =>

    fhddsConnector.getSubMission(formBundleId).map {
      case submission => Ok(show_submission(submission)(request, frontendAppConfig,messagesApi))
      case _ => Ok(s"No Submission found for $formBundleId")
    }
  }

  def deleteSubmission(formBundleId:String): Action[AnyContent] = AuthenticationController(credentials).async { implicit request =>
    fhddsConnector.deleteSubmission(formBundleId).map(_ => Ok(s"Submission data for $formBundleId has been deleted "))
      .recover{case _ => Ok(s"Submission with $formBundleId not found")}

  }

  def loadUserIdPage = AuthenticationController(credentials) { implicit request =>

    Ok(admin_get_groupID(requestForm)(request,frontendAppConfig,messagesApi))
  }

  def sendAdminRequest() = AuthenticationController(credentials).async { implicit request =>
    requestForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(admin_get_groupID(formWithErrors)(request, frontendAppConfig, messagesApi)))
      },

     formData =>
       fhddsConnector.addEnrolment(formData.userId, formData.groupId, formData.registrationNumber).map(result => Ok(result.body))
    )
  }

  def loadAllocateEnrolment = AuthenticationController(credentials) {
    implicit request =>
      Ok(allocate_enrolment(allocateEnrolmentForm)(request, frontendAppConfig, messagesApi))
  }

  def allocateEnrolment = AuthenticationController(credentials).async {
    implicit request  =>
      allocateEnrolmentForm.bindFromRequest.fold (
        formWithErrors => {
          Future.successful(BadRequest(allocate_enrolment(formWithErrors)(request, frontendAppConfig, messagesApi)))
        },

        formData => {
          fhddsConnector.allocateEnrolment(formData.userId, formData.registrationNumber).map(result => Ok(result.body))
        }
      )
  }

  def loadDeleteEnrolment = AuthenticationController(credentials) {
    implicit request =>
      Ok(delete_enrolment(deleteEnrolmentForm)(request, frontendAppConfig, messagesApi))
  }

  def deleteEnrolment = AuthenticationController(credentials).async {
    implicit request =>
      deleteEnrolmentForm.bindFromRequest.fold (
        formWithErrors => {
          Future.successful(BadRequest(delete_enrolment(formWithErrors)(request, frontendAppConfig, messagesApi)))

        },

        formData => {
          fhddsConnector.deleteEnrolment(formData.userId, formData.registrationNumber).map(result => Ok(result.body))
        }
      )
  }


  def checkStatus(regNo: String) = AuthenticationController(credentials).async {
    implicit request  =>
      fhddsConnector
        .getStatus(regNo)(hc).map(result => Ok(result.toString))
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

  def ES2(userId: String) = AuthenticationController(credentials).async {
    implicit request =>
      fhddsConnector.ES2(userId).map{
        response => Ok(response.json)
      }
  }

  def ES3(groupId: String) =  AuthenticationController(credentials).async {
    implicit request =>
      fhddsConnector.ES3(groupId).map{
        response => Ok(response.json)
      }
  }

}

case class AdminRequest(userId:String, groupId:String, registrationNumber: String)

object AdminRequest {
  val format: Format[AdminRequest] = Json.format[AdminRequest]

  val requestForm: Form[AdminRequest] = Form(mapping(
    "userId" -> nonEmptyText,
    "groupId" -> nonEmptyText,
    "registrationNumber" -> nonEmptyText
  )(AdminRequest.apply)(AdminRequest.unapply))
}

case class EnrolmentForm(userId:String, registrationNumber: String)

object EnrolmentForm {

  val format: Format[EnrolmentForm] = Json.format[EnrolmentForm]

  val allocateEnrolmentForm: Form[EnrolmentForm] = Form(mapping(
    "userId" -> nonEmptyText,
    "registrationNumber" -> nonEmptyText
  )(EnrolmentForm.apply)(EnrolmentForm.unapply))

  val deleteEnrolmentForm: Form[EnrolmentForm] = allocateEnrolmentForm
}



