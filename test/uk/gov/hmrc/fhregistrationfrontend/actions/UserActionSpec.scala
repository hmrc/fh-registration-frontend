/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.actions

import java.io.IOException

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{StubbedErrorHandler, StubbedExternalUrls}

import scala.concurrent.Future

class UserActionSpec extends ActionSpecBase {

  val mockAuthConnector = mock[AuthConnector]
  implicit val materializer = mock[Materializer]
  val mockMessagesControllerComponent = mock[MessagesControllerComponents]

  type RetrievalType = Option[String] ~ Option[String] ~ Enrolments ~ Option[CredentialRole] ~ Option[AffinityGroup]

  val fakeRequest: Request[Any] = FakeRequest()
  lazy val action = new UserAction(StubbedExternalUrls, StubbedErrorHandler, mockMessagesControllerComponent)(
    mockAuthConnector,
    scala.concurrent.ExecutionContext.Implicits.global)

  "UserAction" should {
    "Find the internal user id and email with no enrolments " in {

      setupAuthConnector()

      val refined = refinedRequest(action, fakeRequest)
      refined.userId shouldBe "id"
      refined.ggEmail shouldBe Some("email@test.com")
      refined.registrationNumber shouldBe None
      refined.userAffinityGroup shouldBe Some(AffinityGroup.Individual)
      refined.userIsRegistered shouldBe false
    }

    "Find the fhdds Enrolment" in {
      val fhddsEnrolment = EnrolmentIdentifier("EtmpRegistrationNumber", "XZFH00000123456")
      val otherEnrolment = EnrolmentIdentifier("EtmpRegistrationNumber", "XZSDIL000123456")

      val enrolments = Set(
        new Enrolment("HMRC-OBTDS-ORG", Seq(otherEnrolment), "Active"),
        new Enrolment("HMRC-OBTDS-ORG", Seq(fhddsEnrolment), "Active")
      )

      setupAuthConnector(enrolments = enrolments)
      val refined = refinedRequest(action, fakeRequest)
      refined.registrationNumber shouldBe Some("XZFH00000123456")
      refined.userIsRegistered shouldBe true
    }

    "Render the application error page when there are multiple enrolments" in {
      val fhddsEnrolment = EnrolmentIdentifier("EtmpRegistrationNumber", "XZFH00000123456")
      val fhddsEnrolment2 = EnrolmentIdentifier("EtmpRegistrationNumber", "XZFH00000123459")

      val enrolments = Set(
        new Enrolment("HMRC-OBTDS-ORG", Seq(fhddsEnrolment), "Active"),
        new Enrolment("HMRC-OBTDS-ORG", Seq(fhddsEnrolment2), "Active")
      )

      setupAuthConnector(enrolments = enrolments)
      val r = await(result(action, fakeRequest))

      status(r) shouldBe OK
      bodyOf(r) shouldBe "fh.application_error.title"
    }

    "Redirect to gg if user is not logged in" in {
      setupAuthConnector(MissingBearerToken())

      val r = result(action, fakeRequest)
      status(r) shouldBe SEE_OTHER
      redirectLocation(r) shouldBe Some("company/authlogin/path?continue_url=%2Ffhdds&origin=FHDDS")
    }

    "Fail if user id is not defined" in {
      setupAuthConnector(id = None)

      status(result(action, fakeRequest)) shouldBe UNAUTHORIZED
    }

    "Return internal server error on failed connection" in {
      setupAuthConnector(new IOException())
      status(result(action, fakeRequest)) shouldBe INTERNAL_SERVER_ERROR
    }

  }

  def setupAuthConnector(
    id: Option[String] = Some("id"),
    email: Option[String] = Some("email@test.com"),
    enrolments: Set[Enrolment] = Set.empty,
    credentialRole: Option[CredentialRole] = Some(User),
    userAffinityGroup: Option[AffinityGroup] = Some(AffinityGroup.Individual)
  ): OngoingStubbing[Future[RetrievalType]] = {

    val authResult = Future successful (new ~(
      new ~(new ~(new ~(id, email), Enrolments(enrolments)), credentialRole),
      userAffinityGroup))
    when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any())) thenReturn authResult
  }

  def setupAuthConnector(throwable: Throwable) =
    when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any())) thenReturn (Future failed throwable)
}
