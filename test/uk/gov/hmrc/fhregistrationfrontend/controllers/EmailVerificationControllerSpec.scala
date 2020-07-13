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

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.EmailVerificationForm
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, EmailVerificationConnectorMocks, InMemoryShortLivedCache}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers._

class EmailVerificationControllerSpec
    extends ControllerSpecWithGuiceApp with EmailVerificationConnectorMocks with ActionsMock with BeforeAndAfterEach {

  val inMemorySave4Later =
    new Save4LaterService(new InMemoryShortLivedCache(testUserId))(scala.concurrent.ExecutionContext.Implicits.global)
  implicit val hc = HeaderCarrier()
  val controller = new EmailVerificationController(
    commonDependencies,
    mockActions,
    mockMcc,
    mockEmailVerifcationConnector,
    inMemorySave4Later,
    mockViews)(scala.concurrent.ExecutionContext.Implicits.global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockEmailVerifcationConnector,
      mockActions
    )

    inMemorySave4Later.removeUserData(testUserId)
  }

  "contactEmail" should {
    "Render the email_options page" in {
      setupEmailVerificationAction(None, None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.contactEmail)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.emailVerification.label"))
    }
  }

  "forcedContactEmail" should {
    "Render the email_options page" in {
      setupEmailVerificationAction(None, None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.forcedContactEmail)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.emailVerification.forced.label"))
    }
  }

  "submitContactEmail" should {
    "Fail when form has no data" in {
      setupEmailVerificationAction(None, None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.submitContactEmail())(request))

      status(result) shouldBe BAD_REQUEST
      bodyOf(result) should include(Messages("fh.emailVerification.label"))
    }

    "Be successful when the new email is already verified" in {
      setupEmailVerificationAction(None, None)
      setupEmailVerificationConnector("c@c.co", true)

      val request = FakeRequest().withFormUrlEncodedBody(
        EmailVerificationForm.emailOptionKey → "true",
        EmailVerificationForm.defaultEmailKey → "c@c.co"
      )

      val result = await(csrfAddToken(controller.submitContactEmail())(request))

      await(inMemorySave4Later.fetchVerifiedEmail(testUserId)) shouldBe Some("c@c.co")
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/resume")
    }

    "Be successful when the new email is not already verified" in {
      setupEmailVerificationAction(None, None)
      setupEmailVerificationConnector("c@c.co", false)

      val request = FakeRequest().withFormUrlEncodedBody(
        EmailVerificationForm.emailOptionKey → "true",
        EmailVerificationForm.defaultEmailKey → "c@c.co"
      )

      val result = await(csrfAddToken(controller.submitContactEmail())(request))

      await(inMemorySave4Later.fetchVerifiedEmail(testUserId)) shouldBe None
      await(inMemorySave4Later.fetchPendingEmail(testUserId)) shouldBe Some("c@c.co")

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/email-verification-status")
    }
  }

  "submitForcedContactEmail" should {
    "Fail when form has no data" in {
      setupEmailVerificationAction(None, None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.submitForcedContactEmail())(request))

      status(result) shouldBe BAD_REQUEST
      bodyOf(result) should include(Messages("fh.emailVerification.forced.label"))
    }
  }

  "emailVerificationStatus" should {
    "Show email_pending_verification " in {
      setupEmailVerificationAction(None, pendingEmail = Some(ggEmail))

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailVerificationStatus())(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.emailVerification.pending.title"))
    }

    "Redirect to resume when the pending email is verified email" in {
      setupEmailVerificationAction(Some(ggEmail), Some(ggEmail))

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailVerificationStatus())(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/resume")
    }

    "Show email_pending_verification when pending email is not verified email " in {
      setupEmailVerificationAction(verifiedEmail = Some("c@c.co"), pendingEmail = Some(ggEmail))

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailVerificationStatus())(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.emailVerification.pending.title"))
    }

    "Redirect to resume when the email is verified" in {
      setupEmailVerificationAction(Some(ggEmail), None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailVerificationStatus())(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/resume")
    }
  }

  "emailEdit" should {
    "Show the edit form page when there is a verified email" in {
      setupEmailVerificationAction(verifiedEmail = Some("c@c.co"), None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailEdit)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.emailVerification.edit.title"))
    }

    "Show the edit form page when there is a pending email" in {
      setupEmailVerificationAction(verifiedEmail = None, pendingEmail = Some("c@c.co"))

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailEdit)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.emailVerification.edit.title"))
    }

    "Fail when there is no email to edit" in {
      setupEmailVerificationAction(None, None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailEdit)(request))

      status(result) shouldBe BAD_REQUEST

    }
  }

  "emailChange" should {
    "Show the edit form page when there is a verified email" in {
      setupEmailVerificationAction(verifiedEmail = Some("c@c.co"), None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailChange)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.emailVerification.edit.start.title"))
    }

    "Show the edit form page when there is a pending email" in {
      setupEmailVerificationAction(verifiedEmail = None, pendingEmail = Some("c@c.co"))

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailChange)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.emailVerification.edit.start.title"))
    }

    "Fail when there is no email to edit" in {
      setupEmailVerificationAction(None, None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailChange)(request))

      status(result) shouldBe BAD_REQUEST
    }
  }

  "emailVerified" should {
    "Show the email_verified page" in {
      setupEmailVerificationAction(verifiedEmail = Some("c@c.co"), pendingEmail = None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailVerified)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.emailVerification.verified.title"))
    }

    "Failed when no verified email" in {

      setupEmailVerificationAction(None, None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailVerified)(request))

      status(result) shouldBe BAD_REQUEST
    }
  }

  "emailVerify" should {
    "Redirect to email verified and save the email" in {
      setupEmailVerificationAction(None, pendingEmail = Some("c@c.co"))

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailVerify("ACA35F94"))(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/email-verified")
    }

    "Show the email pending verification if the token does not match" in {
      setupEmailVerificationAction(None, pendingEmail = Some("c@c.co"))

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailVerify("123"))(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.emailVerification.pending.title"))
    }

    "Redirect to resume when no actual pending email" in {
      setupEmailVerificationAction(None, None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.emailVerify("123"))(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/resume")
    }
  }
}
