/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package uk.gov.hmrc.fhregistrationfrontend.controllers.admin

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesImpl}
import play.api.mvc.*
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.config.{AppConfig, FrontendAppConfig}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.controllers.{AuthenticationController, Credentials}
import uk.gov.hmrc.fhregistrationfrontend.models.submissiontracking.SubmissionTracking
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import org.mindrot.jbcrypt.BCrypt
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class AdminPageControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  given HeaderCarrier = HeaderCarrier()

  val cc: MessagesControllerComponents = Helpers.stubMessagesControllerComponents()

  val mockFhddsConnector = mock[FhddsConnector]
  val mockFrontendAppConfig = mock[FrontendAppConfig]
  val mockAppConfig = mock[AppConfig]

  val mockTempAdminPage = mock[uk.gov.hmrc.fhregistrationfrontend.views.html.temp_admin_page]
  val mockShowAllSubmissions = mock[uk.gov.hmrc.fhregistrationfrontend.views.html.show_all_submissions]
  val mockShowSubmission = mock[uk.gov.hmrc.fhregistrationfrontend.views.html.show_submission]
  val mockAdminGetGroupID = mock[uk.gov.hmrc.fhregistrationfrontend.views.html.admin_get_groupID]
  val mockAllocateEnrolment = mock[uk.gov.hmrc.fhregistrationfrontend.views.html.allocate_enrolment]
  val mockDeleteEnrolment = mock[uk.gov.hmrc.fhregistrationfrontend.views.html.delete_enrolment]

  val views = new TestViews(
    mockTempAdminPage,
    mockShowAllSubmissions,
    mockShowSubmission,
    mockAdminGetGroupID,
    mockAllocateEnrolment,
    mockDeleteEnrolment
  )

  val testUsername = "admin"
  val testPasswordPlain = "password"
  val hashedPassword = BCrypt.hashpw(testPasswordPlain, BCrypt.gensalt())
  val credentials = Credentials(testUsername, hashedPassword)
  val authAction = AuthenticationController(credentials, cc.parsers.defaultBodyParser)

  val authHeader: String =
    "Basic " + java.util.Base64.getEncoder.encodeToString(s"$testUsername:$testPasswordPlain".getBytes)
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("Authorization" -> authHeader)

  implicit val request: Request[?] = fakeRequest
  implicit val messages: Messages = MessagesImpl(Lang.defaultLang, cc.messagesApi)

  val controller = new AdminPageController(
    mockFrontendAppConfig,
    mockAppConfig,
    mockFhddsConnector,
    cc,
    views
  ) {
    override val authAction = AdminPageControllerSpec.this.authAction
  }


  "AdminPageController.showAdminPage" should {
    "render the temp admin page view" in {
      when(mockTempAdminPage.apply()(using any(), any())).thenReturn(Html("<h1>Admin Page</h1>"))

      val result = controller.showAdminPage(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("Admin Page")
    }
  }

  "AdminPageController.getSubmissions" should {
    "render submissions if available" in {
      when(mockFhddsConnector.getAllSubmission()(using any[HeaderCarrier])).thenReturn(Future.successful(Seq("submission1")))
      when(mockShowAllSubmissions.apply(any())(using any(), any())).thenReturn(Html("<div>Submissions</div>"))

      val result = controller.getSubmissions(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("Submissions")
    }

    "render 'No Submissions found' if none" in {
      when(mockFhddsConnector.getAllSubmission()(using any[HeaderCarrier])).thenReturn(Future.successful(Seq.empty))
      when(mockShowAllSubmissions.apply(any())(using any(), any())).thenReturn(Html("<div>No Submissions found</div>"))

      val result = controller.getSubmissions(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("No Submissions found")
    }
  }

  "AdminPageController.loadDeletePage" should {
    "render a submission if found" in {
      val submission = mock[SubmissionTracking]
      when(mockFhddsConnector.getSubMission(any[String])(using any[HeaderCarrier])).thenReturn(Future.successful(submission))
      when(mockShowSubmission.apply(any())(using any(), any())).thenReturn(Html("<div>Submission</div>"))

      val result = controller.loadDeletePage("123")(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("Submission")
    }

    "render not found if missing" in {
      when(mockFhddsConnector.getSubMission(any[String])(using any[HeaderCarrier])).thenReturn(Future.successful(null))
      val result = controller.loadDeletePage("999")(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("No Submission found for 999")
    }
  }

  "AdminPageController.deleteSubmission" should {
    "delete successfully" in {
      when(mockFhddsConnector.deleteSubmission(any[String])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(status = 200, body = "")))

      val result = controller.deleteSubmission("123")(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("has been deleted")
    }

    "handle not found" in {
      when(mockFhddsConnector.deleteSubmission(any[String])(using any[HeaderCarrier])).
        thenReturn(Future.failed(new Exception("not found")))

      val result = controller.deleteSubmission("999")(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("not found")
    }
  }

  "AdminPageController.loadUserIdPage" should {
    "render the admin get groupID page" in {
      when(mockAdminGetGroupID.apply(any())(using any(), any())).thenReturn(Html("<div>UserId Page</div>"))

      val result = controller.loadUserIdPage(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("UserId Page")
    }
  }

  "AdminPageController.sendAdminRequest" should {
    "return BadRequest for invalid form" in {
      when(mockAdminGetGroupID.apply(any())(using any(), any())).thenReturn(Html("<div>Error Form</div>"))

      val request = fakeRequest.withFormUrlEncodedBody("userId" -> "", "groupId" -> "", "registrationNumber" -> "")
      val result = controller.sendAdminRequest()(request)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("Error Form")
    }
  }

  "AdminPageController.loadAllocateEnrolment" should {
    "render allocate enrolment page" in {
      when(mockAllocateEnrolment.apply(any())(using any(), any())).thenReturn(Html("<div>Allocate</div>"))

      val result = controller.loadAllocateEnrolment(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("Allocate")
    }
  }

  "AdminPageController.allocateEnrolment" should {
    "return BadRequest for invalid form" in {
      when(mockAllocateEnrolment.apply(any())(using any(), any())).thenReturn(Html("<div>Error Allocate</div>"))

      val request = fakeRequest.withFormUrlEncodedBody("userId" -> "", "registrationNumber" -> "")
      val result = controller.allocateEnrolment(request)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("Error Allocate")
    }
  }

  "AdminPageController.loadDeleteEnrolment" should {
    "render delete enrolment page" in {
      when(mockDeleteEnrolment.apply(any())(using any(), any())).thenReturn(Html("<div>Delete Enrolment</div>"))

      val result = controller.loadDeleteEnrolment(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("Delete Enrolment")
    }
  }

  "AdminPageController.deleteEnrolment" should {
    "return BadRequest for invalid form" in {
      when(mockDeleteEnrolment.apply(any())(using any(), any())).thenReturn(Html("<div>Error Delete</div>"))

      val request = fakeRequest.withFormUrlEncodedBody("userId" -> "", "registrationNumber" -> "")
      val result = controller.deleteEnrolment(request)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("Error Delete")
    }
  }

  "AdminPageController.checkStatus" should {
    "return status from connector" in {
      when(mockFhddsConnector.getStatus(any[String])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(FhddsStatus.Approved))

      val result = controller.checkStatus("r1")(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("approved")
    }
  }

  "AdminPageController.getUserInfo" should {
    "return JSON response" in {
      val httpResponse = HttpResponse(200, """{"user":"info"}""")
      when(mockFhddsConnector.getUserInfo(any[String])(using any[HeaderCarrier])).thenReturn(Future.successful(httpResponse))

      val result = controller.getUserInfo("u1")(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("user")
    }
  }

  "AdminPageController.getGroupInfo" should {
    "return JSON response" in {
      val httpResponse = HttpResponse(200, """{"group":"info"}""")
      when(mockFhddsConnector.getGroupInfo(any[String])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(httpResponse))

      val result = controller.getGroupInfo("g1")(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("group")
    }
  }

  "AdminPageController.es2" should {
    "return JSON response" in {
      val httpResponse = HttpResponse(200, """{"es2":"info"}""")
      when(mockFhddsConnector.es2Info(any[String])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(httpResponse))

      val result = controller.es2("u1")(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("es2")
    }
  }

  "AdminPageController.es3" should {
    "return JSON response" in {
      val httpResponse = HttpResponse(200, """{"es3":"info"}""")
      when(mockFhddsConnector.es3Info(any[String])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(httpResponse))


      val result = controller.es3("g1")(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("es3")
    }
  }

}
