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

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.{DeregistrationReason, DeregistrationReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.{WithdrawalReason, WithdrawalReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.models.SummaryConfirmation
import uk.gov.hmrc.fhregistrationfrontend.repositories.SummaryConfirmationRepository
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class SummaryConfirmationServiceSpecs extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with MockitoSugar {
  def createEiSessionService =
    new SummaryConfirmationService(mockKeyStoreService, mockSummaryConfirmationLocalService, mockFhConfig)

  lazy val mockSessionRepository: SummaryConfirmationRepository = mock[SummaryConfirmationRepository]
  lazy val summaryConfirmationLocalService =
    new SummaryConfirmationLocalService(mockSessionRepository, mockFhConfigInstance)
  lazy val mockKeyStoreService: KeyStoreService = mock[KeyStoreService]
  lazy val mockSummaryConfirmationLocalService: SummaryConfirmationLocalService = mock[SummaryConfirmationLocalService]

  lazy val mockFhConfigInstance: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val summaryConfirmationCache: SummaryConfirmation =
    SummaryConfirmation("sessionId", Some("summaryForPrintKey"), None, None)

  val deregistrationReason: DeregistrationReason = DeregistrationReason(
    DeregistrationReasonEnum.NoLongerNeeded,
    Some("saveDeregistrationReason")
  )

  val id = "sessionId"
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(id)))

  lazy val mockFhConfig: FrontendAppConfig = mock[FrontendAppConfig]

  "Summary Confirmation Service" should {

    "isNewSummaryConfirmationCacheEnabled returns true" when {
      "saveSummaryForPrint is called" should {
        "call fhSessionLocalService and return expected data if cache successful" in {
          val sessionService = createEiSessionService
          implicit val hc: HeaderCarrier = HeaderCarrier()

          when(mockFhConfig.isNewSummaryConfirmationCacheEnabled).thenReturn(true)
          when(mockSummaryConfirmationLocalService.saveSummaryForPrint(any())(using any()))
            .thenReturn(Future(Some("summaryForPrintKey")))

          val result = Await.result(sessionService.saveSummaryForPrint("summaryForPrintKey")(using hc), 20 seconds)
          result must be(Some("summaryForPrintKey"))
        }
      }
    }

    "isNewSummaryConfirmationCacheEnabled returns false" when {
      "fetchSummaryForPrint is called" should {
        "call fhSessionKeystoreService and return expected data if cache successful" in {
          val sessionService = createEiSessionService
          implicit val hc: HeaderCarrier = HeaderCarrier()

          when(mockFhConfig.isNewSummaryConfirmationCacheEnabled).thenReturn(false)
          when(mockKeyStoreService.fetchSummaryForPrint()(using any())).thenReturn(Future(Some("summaryForPrintKey")))

          val result = Await.result(sessionService.fetchSummaryForPrint(), 10 seconds)
          result must be(Some("summaryForPrintKey"))
        }
      }
    }

    "return exception if exception thrown in fetchSummaryForPrint" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.failed(new Exception))

      intercept[Exception] {
        val result = Await.result(summaryConfirmationLocalService.fetchSummaryForPrint()(using hc), 20 seconds)
        result match {
          case Some(message) => message must include("Request was successful")
          case None          => fail("Error happened")
        }
      }
    }

    "return exception if exception thrown in fetchWithdrawalReason" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.failed(new Exception))

      intercept[Exception] {
        val result = Await.result(summaryConfirmationLocalService.fetchWithdrawalReason()(using hc), 20 seconds)
        result mustBe new Exception
      }
    }

    "no Exception thrown if future Successful when calling fetchWithdrawalReason" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

      val result = Await.result(summaryConfirmationLocalService.fetchWithdrawalReason()(using hc), 20 seconds)

      result mustBe None
    }

    "return exception if exception thrown in saveSummaryForPrint" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(summaryConfirmationCache)))
      when(mockSessionRepository.set(any())).thenReturn(Future.failed(new Exception))

      intercept[Exception] {
        val result =
          Await.result(summaryConfirmationLocalService.saveSummaryForPrint("summaryForPrintKey")(using hc), 20 seconds)
        result must be(new Exception)
      }
    }

    "return data if future Successful when calling saveSummaryForPrint" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(summaryConfirmationCache)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result =
        Await.result(summaryConfirmationLocalService.saveSummaryForPrint("summaryForPrintKey")(using hc), 20 seconds)
      result mustBe Some("summaryForPrintKey")
    }

    "return exception if exception thrown in saveDeregistrationReason" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(summaryConfirmationCache)))
      when(mockSessionRepository.set(any())).thenReturn(Future.failed(new Exception))

      intercept[Exception] {
        val result =
          Await.result(
            summaryConfirmationLocalService.saveDeregistrationReason(deregistrationReason)(using hc),
            20 seconds
          )
        result must be(new Exception)
      }
    }

    "return data if future Successful when calling saveDeregistrationReason" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(summaryConfirmationCache)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result =
        Await.result(
          summaryConfirmationLocalService.saveDeregistrationReason(deregistrationReason)(using hc),
          20 seconds
        )
      result mustBe Some(deregistrationReason)
    }

    "saveWithdrawalReason should delegate to local service when feature flag is ON" in {
      val sessionService = createEiSessionService
      val reason = WithdrawalReason(WithdrawalReasonEnum.AppliedInError, None)

      when(mockFhConfig.isNewSummaryConfirmationCacheEnabled).thenReturn(true)
      when(mockSummaryConfirmationLocalService.saveWithdrawalReason(eqTo(reason))(using any()))
        .thenReturn(Future.successful(()))

      val result = Await.result(sessionService.saveWithdrawalReason(reason), 10.seconds)
      result mustBe ()
    }

    "saveWithdrawalReason should delegate to keystore service when feature flag is OFF" in {
      val sessionService = createEiSessionService
      val reason = WithdrawalReason(WithdrawalReasonEnum.DuplicateApplication, None)

      when(mockFhConfig.isNewSummaryConfirmationCacheEnabled).thenReturn(false)
      when(mockKeyStoreService.saveWithdrawalReason(eqTo(reason))(using any()))
        .thenReturn(Future.successful(()))

      val result = Await.result(sessionService.saveWithdrawalReason(reason), 10.seconds)
      result mustBe ()
    }

    "fetchWithdrawalReason should delegate to local service when feature flag is ON" in {
      val sessionService = createEiSessionService
      val expected = Some(WithdrawalReason(WithdrawalReasonEnum.Other, Some("Requested by business owner")))

      when(mockFhConfig.isNewSummaryConfirmationCacheEnabled).thenReturn(true)
      when(mockSummaryConfirmationLocalService.fetchWithdrawalReason()(using any()))
        .thenReturn(Future.successful(expected))

      val result = Await.result(sessionService.fetchWithdrawalReason(), 10.seconds)
      result mustBe expected
    }

    "fetchWithdrawalReason should delegate to keystore service when feature flag is OFF" in {
      val sessionService = createEiSessionService
      val expected = Some(WithdrawalReason(WithdrawalReasonEnum.NoLongerApplicable, None))

      when(mockFhConfig.isNewSummaryConfirmationCacheEnabled).thenReturn(false)
      when(mockKeyStoreService.fetchWithdrawalReason()(using any()))
        .thenReturn(Future.successful(expected))

      val result = Await.result(sessionService.fetchWithdrawalReason(), 10.seconds)
      result mustBe expected
    }

    "saveDeregistrationReason should delegate to local service when feature flag is ON" in {
      val sessionService = createEiSessionService
      val reason = DeregistrationReason(DeregistrationReasonEnum.StoppedTrading, None)

      when(mockFhConfig.isNewSummaryConfirmationCacheEnabled).thenReturn(true)
      when(mockSummaryConfirmationLocalService.saveDeregistrationReason(eqTo(reason))(using any()))
        .thenReturn(Future.successful(()))

      val result = Await.result(sessionService.saveDeregistrationReason(reason), 10.seconds)
      result mustBe()
    }

    "saveDeregistrationReason should delegate to keystore service when feature flag is OFF" in {
      val sessionService = createEiSessionService
      val reason = DeregistrationReason(DeregistrationReasonEnum.ChangedLegalEntity, None)

      when(mockFhConfig.isNewSummaryConfirmationCacheEnabled).thenReturn(false)
      when(mockKeyStoreService.saveDeregistrationReason(eqTo(reason))(using any()))
        .thenReturn(Future.successful(()))

      val result = Await.result(sessionService.saveDeregistrationReason(reason), 10.seconds)
      result mustBe()
    }

    "fetchDeregistrationReason should delegate to local service when feature flag is ON" in {
      val sessionService = createEiSessionService
      val expected = Some(DeregistrationReason(DeregistrationReasonEnum.Other, Some("Business transferred to another company")))

      when(mockFhConfig.isNewSummaryConfirmationCacheEnabled).thenReturn(true)
      when(mockSummaryConfirmationLocalService.fetchDeregistrationReason()(using any()))
        .thenReturn(Future.successful(expected))

      val result = Await.result(sessionService.fetchDeregistrationReason(), 10.seconds)
      result mustBe expected
    }

    "fetchDeregistrationReason should delegate to keystore service when feature flag is OFF" in {
      val sessionService = createEiSessionService
      val expected = Some(DeregistrationReason(DeregistrationReasonEnum.NoLongerNeeded, None))

      when(mockFhConfig.isNewSummaryConfirmationCacheEnabled).thenReturn(false)
      when(mockKeyStoreService.fetchDeregistrationReason()(using any()))
        .thenReturn(Future.successful(expected))

      val result = Await.result(sessionService.fetchDeregistrationReason(), 10.seconds)
      result mustBe expected
    }


  }
}
