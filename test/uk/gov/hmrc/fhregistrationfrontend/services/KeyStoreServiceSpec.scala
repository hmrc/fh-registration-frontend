/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.{DeregistrationReason, DeregistrationReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.{WithdrawalReason, WithdrawalReasonEnum}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.SessionCache

import scala.concurrent.Future

class KeyStoreServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with ScalaFutures {

  given HeaderCarrier = HeaderCarrier()

  val mockSessionCache: SessionCache = mock[SessionCache]
  val service = new KeyStoreServiceImpl(mockSessionCache)

  val summaryKey = "fhdds-summary-input"
  val withdrawalKey = "withdrawalReason"
  val deregistrationKey = "deregistrationReason"

  "KeyStoreService.saveSummaryForPrint" should {
    "cache the summary string" in {
      val summary = "Summary text"

      when(mockSessionCache.cache(eqTo(summaryKey), eqTo(summary))(using any(), any(), any()))
        .thenReturn(Future.successful(mock[uk.gov.hmrc.http.cache.client.CacheMap]))

      service.saveSummaryForPrint(summary).map { _ =>
        succeed
      }
    }
  }

  "KeyStoreService.fetchSummaryForPrint" should {
    "return the summary string from the cache" in {
      val summary = "Cached summary"

      when(mockSessionCache.fetchAndGetEntry[String](eqTo(summaryKey))(using any(), any(), any()))
        .thenReturn(Future.successful(Some(summary)))

      service.fetchSummaryForPrint().map { result =>
        result shouldBe Some(summary)
      }
    }
  }

  "KeyStoreService.saveWithdrawalReason" should {
    "cache the withdrawal reason" in {
      val reason = WithdrawalReason(WithdrawalReasonEnum.AppliedInError, Some("Wrong submission"))

      when(mockSessionCache.cache(eqTo(withdrawalKey), eqTo(reason))(using any(), any(), any()))
        .thenReturn(Future.successful(mock[uk.gov.hmrc.http.cache.client.CacheMap]))

      service.saveWithdrawalReason(reason).map { _ =>
        succeed
      }
    }
  }

  "KeyStoreService.fetchWithdrawalReason" should {
    "return the withdrawal reason from the cache" in {
      val reason = WithdrawalReason(WithdrawalReasonEnum.DuplicateApplication, Some("Duplicate"))

      when(mockSessionCache.fetchAndGetEntry[WithdrawalReason](eqTo(withdrawalKey))(using any(), any(), any()))
        .thenReturn(Future.successful(Some(reason)))

      service.fetchWithdrawalReason().map { result =>
        result shouldBe Some(reason)
      }
    }
  }

  "KeyStoreService.saveDeregistrationReason" should {
    "cache the deregistration reason" in {
      val reason = DeregistrationReason(DeregistrationReasonEnum.NoLongerNeeded, Some("Business closed"))

      when(mockSessionCache.cache(eqTo(deregistrationKey), eqTo(reason))(using any(), any(), any()))
        .thenReturn(Future.successful(mock[uk.gov.hmrc.http.cache.client.CacheMap]))

      service.saveDeregistrationReason(reason).map { _ =>
        succeed
      }
    }
  }

  "KeyStoreService.fetchDeregistrationReason" should {
    "return the deregistration reason from the cache" in {
      val reason = DeregistrationReason(DeregistrationReasonEnum.ChangedLegalEntity, Some("Switched to partnership"))

      when(mockSessionCache.fetchAndGetEntry[DeregistrationReason](eqTo(deregistrationKey))(using any(), any(), any()))
        .thenReturn(Future.successful(Some(reason)))

      service.fetchDeregistrationReason().map { result =>
        result shouldBe Some(reason)
      }
    }
  }
}
