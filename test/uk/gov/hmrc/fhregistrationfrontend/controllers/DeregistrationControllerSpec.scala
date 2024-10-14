/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.forms.confirmation.ConfirmationForm
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.{DeregistrationReason, DeregistrationReasonEnum, DeregistrationReasonForm}
import uk.gov.hmrc.fhregistrationfrontend.services.SummaryConfirmationService
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.DesToFormImpl
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, FhddsConnectorMocks}

import scala.concurrent.Future

class DeregistrationControllerSpec
    extends ControllerSpecWithGuiceApp with FhddsConnectorMocks with ActionsMock with BeforeAndAfterEach {

  val desToForm = new DesToFormImpl()
  val mockKeyStoreService = mock[SummaryConfirmationService]

  val controller = new DeregistrationController(
    commonDependencies,
    mockFhddsConnector,
    desToForm,
    mockKeyStoreService,
    mockMcc,
    mockActions,
    views
  )(scala.concurrent.ExecutionContext.Implicits.global)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(mockKeyStoreService, mockFhddsConnector, mockActions)
  }

  override def beforeEach() = {
    super.beforeEach()
    setupSaveDeregistrationReason()
    setupEnrolledUserAction()
    setupDesDisplayResult()
  }

  "Deregistration Controller" should {
    "Redirect to reason on start" in {
      val result = controller.startDeregister.apply(FakeRequest())

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/deregistration/reason")
    }

    "Render reason page" in {
//      when(mockViews.deregistration_reason) thenReturn uk.gov.hmrc.fhregistrationfrontend.views.html.deregistration_reason
      val result = csrfAddToken(controller.reason).apply(FakeRequest())

      status(result) shouldBe OK
      contentAsString(result) should include(Messages("fh.deregistration.title"))
    }

    "Fail on postReason when reason is not given" in {
      val result = csrfAddToken(controller.postReason)(FakeRequest())

      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(Messages("fh.deregistration.title"))
      contentAsString(result) should include(Messages("fh.generic.errorPrefix"))
    }

    "Redirect from postReason to confirm when reason is given" in {
      val request = FakeRequest()
        .withFormUrlEncodedBody(
          DeregistrationReasonForm.reasonKey -> DeregistrationReasonEnum.NoLongerNeeded.toString
        )
        .withMethod("POST")
      val result = csrfAddToken(controller.postReason)(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/deregistration/confirm")
    }

    "Render the confirmation page if all data is given" in {
      val request = FakeRequest()
      setupKeyStoreDeregistrationReason()
      val result = csrfAddToken(controller.confirm)(request)

      status(result) shouldBe OK
      contentAsString(result) should include(Messages("fh.deregistrationConfirm.title"))
    }

    "Fail to render confirmation if reason was not given previously" in {
      setupKeyStoreDeregistrationReason(None)
      val request = FakeRequest()
      val result = csrfAddToken(controller.confirm)(request)

      status(result) shouldBe BAD_REQUEST
    }

    "Fail on postConfirmation if confirmation is not answered" in {
      setupKeyStoreDeregistrationReason()
      val request = FakeRequest()
      val result = csrfAddToken(controller.postConfirmation)(request)

      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(Messages("fh.deregistrationConfirm.title"))
      contentAsString(result) should include(Messages("fh.generic.errorPrefix"))
    }

    "Fail on postConfirmation if reason was not saved" in {
      setupKeyStoreDeregistrationReason(None)
      val request = FakeRequest().withFormUrlEncodedBody(
        ConfirmationForm.confirmKey           -> "true",
        ConfirmationForm.usingDefaultEmailKey -> "true",
        ConfirmationForm.defaultEmailKey      -> "some@email.com"
      )
      val result = csrfAddToken(controller.postConfirmation)(request)

      status(result) shouldBe BAD_REQUEST
    }

    "Redirect from postConfirmation if all data is give" in {
      setupKeyStoreDeregistrationReason()
      setupDeregister()
      val request = FakeRequest()
        .withFormUrlEncodedBody(
          ConfirmationForm.confirmKey           -> "true",
          ConfirmationForm.usingDefaultEmailKey -> "true",
          ConfirmationForm.defaultEmailKey      -> "some@email.com"
        )
        .withMethod("POST")
      val result = csrfAddToken(controller.postConfirmation)(request)

      status(result) shouldBe SEE_OTHER
      val s = session(result)
      s.get(controller.EmailSessionKey) shouldBe Some("some@email.com")
      s.get(controller.ProcessingTimestampSessionKey) shouldBe defined
      redirectLocation(result) shouldBe Some("/fhdds/deregistration/acknowledgment")
    }

    "Redirect from postConfirmation if tne answer is no" in {
      setupKeyStoreDeregistrationReason()
      setupDeregister()
      val request = FakeRequest()
        .withFormUrlEncodedBody(
          ConfirmationForm.confirmKey -> "false"
        )
        .withMethod("POST")
      val result = csrfAddToken(controller.postConfirmation)(request)

      status(result) shouldBe SEE_OTHER
      session(result)
      redirectLocation(result) shouldBe Some("/fhdds/subscription/status")
    }

    "Fail to render the ack page is session does not have the required keys" in {
      setupUserAction()
      val request = FakeRequest().withSession()
      val result = csrfAddToken(controller.acknowledgment)(request)
      status(result) shouldBe NOT_FOUND
    }

    "Render the ack page is session does not have the required keys" in {
      setupUserAction()
      val request = FakeRequest().withSession(
        controller.EmailSessionKey               -> "some@email.com",
        controller.ProcessingTimestampSessionKey -> System.currentTimeMillis.toString
      )
      val result = csrfAddToken(controller.acknowledgment)(request)
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("fh.ack.deregister"))
    }
  }

  def setupSaveDeregistrationReason() =
    when(mockKeyStoreService.saveDeregistrationReason(any())(any())) thenReturn Future.successful(())

  def setupKeyStoreDeregistrationReason(
    reason: Option[DeregistrationReason] = Some(DeregistrationReason(DeregistrationReasonEnum.NoLongerNeeded, None))
  ): Unit =
    when(mockKeyStoreService.fetchDeregistrationReason()(any())) thenReturn Future.successful(reason)
}
