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

import scala.concurrent.ExecutionContext.Implicits.global
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.models.SummaryConfirmation
import uk.gov.hmrc.fhregistrationfrontend.repositories.SummaryConfirmationRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.SessionId

import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class SummaryConfirmationServiceSpecs extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with MockitoSugar {

  lazy val mockSessionRepository: SummaryConfirmationRepository = mock[SummaryConfirmationRepository]
  lazy val summaryConfirmationService = new SummaryConfirmationService(mockSessionRepository, mockEiConfig)

  lazy val mockEiConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val summaryConfirmationCache: SummaryConfirmation =
    SummaryConfirmation("sessionId", Some("summaryForPrintKey"), None, None)

  val id = "sessionId"
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(id)))

  "Summary Confirmation Service" should {

    "return exception if exception thrown in fetchSummaryForPrint" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.failed(new Exception))

      intercept[Exception] {
        val result = Await.result(summaryConfirmationService.fetchSummaryForPrint()(hc), 20 seconds)
        result mustBe new Exception
      }
    }

    "return exception if exception thrown in fetchWithdrawalReason" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.failed(new Exception))

      intercept[Exception] {
        val result = Await.result(summaryConfirmationService.fetchWithdrawalReason()(hc), 20 seconds)
        result mustBe new Exception
      }
    }

    "no Exception thrown if future Successful when calling fetchWithdrawalReason" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

      val result = Await.result(summaryConfirmationService.fetchWithdrawalReason()(hc), 20 seconds)

      result mustBe None
    }

    "return exception if exception thrown in saveSummaryForPrint" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(summaryConfirmationCache)))
      when(mockSessionRepository.set(any())).thenReturn(Future.failed(new Exception))

      intercept[Exception] {
        val result = Await.result(summaryConfirmationService.saveSummaryForPrint("summaryForPrintKey")(hc), 20 seconds)
        result must be(new Exception)
      }
    }

    "return agent data if future Successful when calling saveSummaryForPrint" in {

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(summaryConfirmationCache)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result = Await.result(summaryConfirmationService.saveSummaryForPrint("summaryForPrintKey")(hc), 20 seconds)
      result mustBe Some("summaryForPrintKey")
    }
  }

}
