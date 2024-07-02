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
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.{WithdrawalReason, WithdrawalReasonEnum, WithdrawalReasonForm}
import uk.gov.hmrc.fhregistrationfrontend.services.KeyStoreService
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.DesToFormImpl
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, FhddsConnectorMocks}
import scala.concurrent.Future

class WithdrawalControllerSpec
    extends ControllerSpecWithGuiceApp with FhddsConnectorMocks with ActionsMock with BeforeAndAfterEach {

  val desToForm = new DesToFormImpl()
  val mockKeyStoreService = mock[KeyStoreService]

  val controller = new WithdrawalController(
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
    setupSaveWithdrawalReason()
    setupEnrolledUserAction()
    setupDesDisplayResult()
  }

  "Withdrawal Controller" should {
    "Redirect to reason on start" in {
      val result = controller.startWithdraw.apply(FakeRequest())

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/withdraw/reason")
    }

    "Render reason page" in {

      val result = csrfAddToken(controller.reason).apply(FakeRequest())

      status(result) shouldBe OK
      contentAsString(result) should include(Messages("fh.withdrawalReason.title"))
    }

    "Fail on postReason when reason is not given" in {
      val result = csrfAddToken(controller.postReason)(FakeRequest())

      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(Messages("fh.withdrawalReason.title"))
      contentAsString(result) should include(Messages("fh.generic.errorPrefix"))
    }

    "Redirect from postReason to confirm when reason is given" in {
      val request = FakeRequest()
        .withFormUrlEncodedBody(
          WithdrawalReasonForm.reasonKey -> WithdrawalReasonEnum.AppliedInError.toString
        )
        .withMethod("POST")
      val result = csrfAddToken(controller.postReason)(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/withdraw/confirm")
    }

    "Render the confirmation page if all data is given" in {
      val request = FakeRequest()
      setupKeyStoreWithdrawalReason()
      val result = csrfAddToken(controller.confirm)(request)

      status(result) shouldBe OK
      contentAsString(result) should include(Messages("fh.withdrawalConfirm.title"))
    }

    "Fail to render confirmation if reason was not given previously" in {
      setupKeyStoreWithdrawalReason(None)
      val request = FakeRequest()
      val result = csrfAddToken(controller.confirm)(request)

      status(result) shouldBe BAD_REQUEST
    }

    "Fail on postConfirmation if confirmation is not answered" in {
      setupKeyStoreWithdrawalReason()
      val request = FakeRequest()
      val result = csrfAddToken(controller.postConfirmation)(request)

      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(Messages("fh.withdrawalConfirm.title"))
      contentAsString(result) should include(Messages("fh.generic.errorPrefix"))
    }

    "Fail on postConfirmation if reason was not saved" in {
      setupKeyStoreWithdrawalReason(None)
      val request = FakeRequest().withFormUrlEncodedBody(
        ConfirmationForm.confirmKey           -> "true",
        ConfirmationForm.usingDefaultEmailKey -> "true",
        ConfirmationForm.defaultEmailKey      -> "some@email.com"
      )
      val result = csrfAddToken(controller.postConfirmation)(request)

      status(result) shouldBe BAD_REQUEST
    }

    "Redirect from postConfirmation if all data is give" in {
      setupKeyStoreWithdrawalReason()
      setupWithdrawal()
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
      redirectLocation(result) shouldBe Some("/fhdds/withdraw/acknowledgment")
    }

    "Redirect from postConfirmation if tne answer is no" in {
      setupKeyStoreWithdrawalReason()
      setupWithdrawal()
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
      contentAsString(result) should include(Messages("fh.ack.withdrawal"))

    }
  }

  def setupSaveWithdrawalReason() =
    when(mockKeyStoreService.saveWithdrawalReason(any())(any())) thenReturn Future.successful(())

  def setupKeyStoreWithdrawalReason(
    reason: Option[WithdrawalReason] = Some(WithdrawalReason(WithdrawalReasonEnum.AppliedInError, None))
  ): Unit =
    when(mockKeyStoreService.fetchWithdrawalReason()(any())) thenReturn Future.successful(reason)
}
