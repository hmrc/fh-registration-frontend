/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.{DeregistrationReason, DeregistrationReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.{WithdrawalReason, WithdrawalReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.models.SummaryConfirmation
import uk.gov.hmrc.fhregistrationfrontend.repositories.SummaryConfirmationRepository
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.{ExecutionContext, Future}

class SummaryConfirmationLocalServiceSpec
    extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("test-session-id")))
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val mockSessionRepository = mock[SummaryConfirmationRepository]
  val mockConfig = mock[FrontendAppConfig]

  when(mockConfig.serviceMaxNoOfAttempts).thenReturn(1)

  val service = new SummaryConfirmationLocalService(mockSessionRepository, mockConfig)

  val summaryForPrintKeyTestData = "some-html-summary"
  val sessionIdExampleTestData = "test-session-id"
  val withdrawalReasonTestData: WithdrawalReason =
    WithdrawalReason(WithdrawalReasonEnum.NoLongerApplicable, Some("testData"))
  val deregistrationReasonTestData: DeregistrationReason =
    DeregistrationReason(DeregistrationReasonEnum.NoLongerNeeded, Some("testData"))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    when(mockConfig.serviceMaxNoOfAttempts).thenReturn(1)
  }

  "saveSummaryForPrint" should {

    "return Some(summaryForPrint) when session exists and save succeeds" in {
      val existingCache = SummaryConfirmation(
        id = sessionIdExampleTestData,
        summaryForPrintKey = None,
        withdrawalReason = None,
        deregistrationReason = None
      )

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(Some(existingCache)))

      when(mockSessionRepository.set(any[SummaryConfirmation]()))
        .thenReturn(Future.successful(true))

      val result = service.saveSummaryForPrint(summaryForPrintKeyTestData).futureValue

      result shouldBe Some(summaryForPrintKeyTestData)

      verify(mockSessionRepository).set(
        existingCache.copy(summaryForPrintKey = Some(summaryForPrintKeyTestData))
      )
    }

    "return Some(summaryForPrint) when no session exists and save succeeds" in {
      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(None))

      when(mockSessionRepository.set(any[SummaryConfirmation]()))
        .thenReturn(Future.successful(true))

      val result = service.saveSummaryForPrint(summaryForPrintKeyTestData).futureValue

      result shouldBe Some(summaryForPrintKeyTestData)

      verify(mockSessionRepository).set(
        SummaryConfirmation(sessionIdExampleTestData, Some(summaryForPrintKeyTestData), None, None)
      )
    }

    "return None when setSessionCacheDataWithRetry fails" in {
      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, None)

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(Some(existingCache)))

      when(mockSessionRepository.set(any[SummaryConfirmation]()))
        .thenReturn(Future.failed(new RuntimeException("mongo is down")))

      val result = service.saveSummaryForPrint(summaryForPrintKeyTestData).futureValue

      result shouldBe None
    }
  }

  "fetchSummaryForPrint" should {

    "return Some(summaryForPrintKey) when maxNoOfAttempts hasn't been exhausted" in {

      val existingCache = SummaryConfirmation(
        id = sessionIdExampleTestData,
        summaryForPrintKey = Some("my-summary-html"),
        withdrawalReason = None,
        deregistrationReason = None
      )

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(Some(existingCache)))

      val result = service.fetchSummaryForPrint().futureValue

      result shouldBe Some("my-summary-html")
    }

    "return None when invalid summaryID is provided" in {
      val existingCache = SummaryConfirmation(
        id = sessionIdExampleTestData,
        summaryForPrintKey = None,
        withdrawalReason = None,
        deregistrationReason = None
      )

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(None))

      val result = service.fetchSummaryForPrint().futureValue

      result shouldBe None
    }

    "return None when sessionIdExampleTestData is valid but summaryPrintKey is not set" in {
      val existingCache = SummaryConfirmation(
        id = sessionIdExampleTestData,
        summaryForPrintKey = None,
        withdrawalReason = None,
        deregistrationReason = None
      )

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(Some(existingCache)))

      val result = service.fetchSummaryForPrint().futureValue

      result shouldBe None
    }
  }

  "saveWithdrawlReason" should {

    "return Some(WithdrawalReason) when session exists and save succeeds" in {
      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, None)

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(Some(existingCache)))

      when(mockSessionRepository.set(any[SummaryConfirmation]))
        .thenReturn(Future.successful(true))

      val result = service.saveWithdrawalReason(withdrawalReasonTestData).futureValue

      result shouldBe Some(withdrawalReasonTestData)

      verify(mockSessionRepository).set(existingCache.copy(withdrawalReason = Some(withdrawalReasonTestData)))
    }

    "return Some(withdrawlReason) when no session exists and save succeeds" in {

      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, None)

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(None))

      when(mockSessionRepository.set(any[SummaryConfirmation]))
        .thenReturn(Future.successful(true))

      val result = service.saveWithdrawalReason(withdrawalReasonTestData).futureValue

      result shouldBe Some(withdrawalReasonTestData)

      verify(mockSessionRepository).set(existingCache.copy(withdrawalReason = Some(withdrawalReasonTestData)))

    }

    "return None when setSessionCacheDataWithRetry fails" in {

      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, None)

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(None))

      when(mockSessionRepository.set(any[SummaryConfirmation]))
        .thenReturn(Future.failed(new RuntimeException("mongo is down")))

      val result = service.saveWithdrawalReason(withdrawalReasonTestData).futureValue

      result shouldBe None

    }
  }

  "fetchWithdrawlReason" should {

    "return Some(withdrawalReason) when maxNoOfAttempts hasn't been exhausted" in {

      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, Some(withdrawalReasonTestData), None)

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(Some(existingCache)))

      val result = service.fetchWithdrawalReason().futureValue

      result shouldBe Some(withdrawalReasonTestData)
    }

    "return None when invalid summaryID is provided" in {

      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, None)

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(None))

      val result = service.fetchWithdrawalReason().futureValue

      result shouldBe None
    }

    "return None when sessionIdExampleTestData is valid but withdrawalReason is not set" in {

      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, None)

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(Some(existingCache)))

      val result = service.fetchWithdrawalReason().futureValue

      result shouldBe None
    }

  }

  "saveDeregistrationReason" should {

    "return Some(deregistrationReason) when session exists and save succeeds" in {

      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, Some(deregistrationReasonTestData))

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(Some(existingCache)))

      when(mockSessionRepository.set(any[SummaryConfirmation]))
        .thenReturn(Future.successful(true))

      val result = service.saveDeregistrationReason(deregistrationReasonTestData).futureValue

      result shouldBe Some(deregistrationReasonTestData)

      verify(mockSessionRepository).set(existingCache.copy(deregistrationReason = Some(deregistrationReasonTestData)))
    }

    "return Some(deregistrationReason) when no session exists and save succeeds" in {

      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, None)

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(None))

      when(mockSessionRepository.set(any[SummaryConfirmation]))
        .thenReturn(Future.successful(true))

      val result = service.saveDeregistrationReason(deregistrationReasonTestData).futureValue

      result shouldBe Some(deregistrationReasonTestData)

      verify(mockSessionRepository).set(existingCache.copy(deregistrationReason = Some(deregistrationReasonTestData)))

    }

    "return None when setSessionCacheDataWithRetry fails" in {
      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, None)

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(None))

      when(mockSessionRepository.set(any[SummaryConfirmation]))
        .thenReturn(Future.failed(new RuntimeException("mongo is down")))

      val result = service.saveDeregistrationReason(deregistrationReasonTestData).futureValue

      result shouldBe None

    }
  }

  "fetchDeregistrationReason" should {

    "return Some(deregistrationTestData) when maxNoOfAttempts hasn't been exhausted" in {

      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, Some(deregistrationReasonTestData))

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(Some(existingCache)))

      val result = service.fetchDeregistrationReason().futureValue

      result shouldBe Some(deregistrationReasonTestData)

    }

    "return None when invalid summaryID is provided" in {

      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, None)

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(None))

      val result = service.fetchDeregistrationReason().futureValue

      result shouldBe None

    }

    "return None when sessionId is valid but deregistration is not set" in {

      val existingCache = SummaryConfirmation(sessionIdExampleTestData, None, None, None)

      when(mockSessionRepository.get(sessionIdExampleTestData))
        .thenReturn(Future.successful(None))

      val result = service.fetchDeregistrationReason().futureValue

      result shouldBe None
    }
  }

}
