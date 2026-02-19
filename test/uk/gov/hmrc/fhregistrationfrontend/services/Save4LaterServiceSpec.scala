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

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.models.des
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.{Address, BusinessRegistrationDetails, Identification}
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import models.UserAnswers
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class Save4LaterServiceSpec
    extends AsyncWordSpec with Matchers with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  given HeaderCarrier = HeaderCarrier()

  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockAppConfig: AppConfig = mock[AppConfig]
  val service = new Save4LaterService(mockCache, mockSessionRepository, mockAppConfig)

  private def stubFetch(userId: String, data: Map[String, JsValue]): Unit =
    when(mockCache.fetch(eqTo(userId))(using any(), any()))
      .thenReturn(Future.successful(Some(CacheMap(userId, data))))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCache, mockSessionRepository, mockAppConfig)
    when(mockAppConfig.isNewSessionRepositoryCacheEnabled).thenReturn(false)
  }

  "Save4LaterService.saveBusinessType" should {
    "cache the business type and timestamp" in {
      val userId = "user-123"
      val businessType: BusinessType.Value = BusinessType.SoleTrader
      val formId = Save4LaterKeys.businessTypeKey

      val cacheMap = CacheMap(userId, Map(formId -> Json.toJson(businessType)))

      when(
        mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any())
      )
        .thenReturn(Future.successful(cacheMap))

      when(mockCache.cache(eqTo(userId), eqTo(formId), eqTo(businessType))(using any(), any(), any()))
        .thenReturn(Future.successful(cacheMap))

      service.saveBusinessType(userId, businessType).map { result =>
        result shouldBe Some(businessType)
      }
    }

    "when new session repository cache is enabled, persist to session repository and also write to short-lived cache" in {
      when(mockAppConfig.isNewSessionRepositoryCacheEnabled).thenReturn(true)

      val userId = "user-123"
      val businessType: BusinessType.Value = BusinessType.SoleTrader
      val formId = Save4LaterKeys.businessTypeKey

      val cacheMap = CacheMap(userId, Map(formId -> Json.toJson(businessType)))

      when(mockSessionRepository.setEntries(eqTo(userId), any[Map[String, JsValue]]))
        .thenReturn(Future.successful(true))

      when(
        mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any())
      )
        .thenReturn(Future.successful(cacheMap))

      when(mockCache.cache(eqTo(userId), eqTo(formId), eqTo(businessType))(using any(), any(), any()))
        .thenReturn(Future.successful(cacheMap))

      val captor = ArgumentCaptor.forClass(classOf[Map[String, JsValue]])

      service.saveBusinessType(userId, businessType).map { result =>
        result shouldBe Some(businessType)

        verify(mockSessionRepository).setEntries(eqTo(userId), captor.capture())
        val savedEntries = captor.getValue
        savedEntries(formId) shouldBe Json.toJson(businessType)
        savedEntries(Save4LaterKeys.userLastTimeSavedKey).asOpt[Long].isDefined shouldBe true

        verify(mockCache)
          .cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any())
        verify(mockCache).cache(eqTo(userId), eqTo(formId), eqTo(businessType))(using any(), any(), any())

        succeed
      }
    }

    "when session repository write fails, still write to short-lived cache" in {
      when(mockAppConfig.isNewSessionRepositoryCacheEnabled).thenReturn(true)

      val userId = "user-err"
      val businessType: BusinessType.Value = BusinessType.SoleTrader
      val formId = Save4LaterKeys.businessTypeKey

      val cacheMap = CacheMap(userId, Map(formId -> Json.toJson(businessType)))

      when(mockSessionRepository.setEntries(eqTo(userId), any[Map[String, JsValue]]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      when(
        mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any())
      )
        .thenReturn(Future.successful(cacheMap))

      when(mockCache.cache(eqTo(userId), eqTo(formId), eqTo(businessType))(using any(), any(), any()))
        .thenReturn(Future.successful(cacheMap))

      service.saveBusinessType(userId, businessType).map { result =>
        result shouldBe Some(businessType)

        verify(mockSessionRepository).setEntries(eqTo(userId), any[Map[String, JsValue]])
        verify(mockCache)
          .cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any())
        verify(mockCache).cache(eqTo(userId), eqTo(formId), eqTo(businessType))(using any(), any(), any())

        succeed
      }
    }
  }

  "Save4LaterService.fetchBusinessType" should {
    "return the cached business type" in {
      val userId = "user-123"
      val formId = Save4LaterKeys.businessTypeKey
      val businessType = BusinessType.CorporateBody

      stubFetch(userId, Map(formId -> Json.toJson(businessType.toString)))

      service.fetchBusinessType(userId).map { result =>
        result shouldBe Some(businessType.toString)
      }
    }
  }

  "Save4LaterService.saveVerifiedEmail" should {
    "cache the verified email" in {
      val userId = "user-456"
      val email = "test@example.com"
      val formId = Save4LaterKeys.verifiedEmailKey

      val cacheMap = CacheMap(userId, Map(formId -> Json.toJson(email)))

      when(
        mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any())
      )
        .thenReturn(Future.successful(cacheMap))

      when(mockCache.cache(eqTo(userId), eqTo(formId), eqTo(email))(using any(), any(), any()))
        .thenReturn(Future.successful(cacheMap))

      service.saveVerifiedEmail(userId, email).map { result =>
        result shouldBe Some(email)
      }
    }
  }

  "Save4LaterService.fetchVerifiedEmail" should {
    "return the cached email" in {
      val userId = "user-456"
      val email = "test@example.com"

      stubFetch(userId, Map(Save4LaterKeys.verifiedEmailKey -> Json.toJson(email)))

      service.fetchVerifiedEmail(userId).map { result =>
        result shouldBe Some(email)
      }
    }
  }

  "Save4LaterService.saveBusinessRegistrationDetails" should {
    "cache the business registration details" in {
      val userId = "user-789"
      val address = Address("10 Downing St", "Westminster", Some("London"), None, Some("SW1A 2AA"), "GB")
      val brd = BusinessRegistrationDetails(
        businessName = Some("Test Ltd"),
        businessType = Some("Limited Company"),
        businessAddress = address,
        safeId = Some("SAFE123")
      )

      val cacheMap = CacheMap(userId, Map(Save4LaterKeys.businessRegistrationDetailsKey -> Json.toJson(brd)))

      when(
        mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any())
      )
        .thenReturn(Future.successful(cacheMap))

      when(
        mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.businessRegistrationDetailsKey), eqTo(brd))(
          using any(),
          any(),
          any()
        )
      )
        .thenReturn(Future.successful(cacheMap))

      service.saveBusinessRegistrationDetails(userId, brd).map { result =>
        result shouldBe Some(brd)
      }
    }
  }

  "Save4LaterService.fetchBusinessRegistrationDetails" should {
    "return the cached business registration details" in {
      val userId = "user-789"
      val address = Address("10 Downing St", "Westminster", Some("London"), None, Some("SW1A 2AA"), "GB")
      val brd = BusinessRegistrationDetails(
        businessName = Some("Test Ltd"),
        businessType = Some("Limited Company"),
        businessAddress = address,
        safeId = Some("SAFE123")
      )

      stubFetch(userId, Map(Save4LaterKeys.businessRegistrationDetailsKey -> Json.toJson(brd)))

      service.fetchBusinessRegistrationDetails(userId).map { result =>
        result shouldBe Some(brd)
      }
    }
  }

  "Save4LaterService.savePendingEmail" should {
    "cache the pending email" in {
      val userId = "user-321"
      val email = "pending@example.com"
      val formId = Save4LaterKeys.pendingEmailKey

      val cacheMap = CacheMap(userId, Map(formId -> Json.toJson(email)))

      when(
        mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any())
      )
        .thenReturn(Future.successful(cacheMap))

      when(mockCache.cache(eqTo(userId), eqTo(formId), eqTo(email))(using any(), any(), any()))
        .thenReturn(Future.successful(cacheMap))

      service.savePendingEmail(userId, email).map { result =>
        result shouldBe Some(email)
      }
    }
  }

  "Save4LaterService.fetchPendingEmail" should {
    "return the cached pending email (non-empty)" in {
      val userId = "user-321"
      val email = "pending@example.com"

      stubFetch(userId, Map(Save4LaterKeys.pendingEmailKey -> Json.toJson(email)))

      service.fetchPendingEmail(userId).map { result =>
        result shouldBe Some(email)
      }
    }

    "return None if pending email is an empty string" in {
      val userId = "user-321"

      stubFetch(userId, Map(Save4LaterKeys.pendingEmailKey -> Json.toJson("")))

      service.fetchPendingEmail(userId).map { result =>
        result shouldBe None
      }
    }
  }

  "Save4LaterService.deletePendingEmail" should {
    "clear the pending email by saving an empty string" in {
      val userId = "user-321"
      val cacheMap = CacheMap(userId, Map(Save4LaterKeys.pendingEmailKey -> Json.toJson("")))

      when(
        mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any())
      )
        .thenReturn(Future.successful(cacheMap))

      when(mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.pendingEmailKey), eqTo(""))(using any(), any(), any()))
        .thenReturn(Future.successful(cacheMap))

      service.deletePendingEmail(userId).map { result =>
        result shouldBe Some("")
      }
    }
  }

  "Save4LaterService.saveV1ContactEmail" should {
    "cache the v1 contact email" in {
      val userId = "user-654"
      val email = "v1@example.com"
      val formId = Save4LaterKeys.v1ContactEmailKey
      val cacheMap = CacheMap(userId, Map(formId -> Json.toJson(email)))

      when(
        mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any())
      )
        .thenReturn(Future.successful(cacheMap))

      when(mockCache.cache(eqTo(userId), eqTo(formId), eqTo(email))(using any(), any(), any()))
        .thenReturn(Future.successful(cacheMap))

      service.saveV1ContactEmail(userId, email).map { result =>
        result shouldBe Some(email)
      }
    }
  }

  "Save4LaterService.fetchV1ContactEmail" should {
    "return the cached v1 contact email" in {
      val userId = "user-654"
      val email = "v1@example.com"

      stubFetch(userId, Map(Save4LaterKeys.v1ContactEmailKey -> Json.toJson(email)))

      service.fetchV1ContactEmail(userId).map { result =>
        result shouldBe Some(email)
      }
    }
  }

  "Save4LaterService.saveJourneyType" should {
    "cache the journey type correctly" in {
      val userId = "user-journey"
      val journeyType = JourneyType.Amendment
      val cacheMap = CacheMap(userId, Map(Save4LaterKeys.journeyTypeKey -> Json.toJson(journeyType)))

      when(
        mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.journeyTypeKey), eqTo(journeyType))(using any(), any(), any())
      ).thenReturn(Future.successful(cacheMap))

      service.saveJourneyType(userId, journeyType).map { result =>
        result shouldBe Some(journeyType)
      }
    }
  }

  "Save4LaterService.saveDisplayData4Later" should {
    "cache the display data using prefixed display key" in {
      val userId = "user-display"
      val formId = "declaration"
      val declaration = des.Declaration(
        personName = "John Smith",
        personStatus = "Director",
        email = Some("john.smith@example.com"),
        isInformationAccurate = true
      )
      val displayKey = Save4LaterKeys.displayKeyForPage(formId)
      val cacheMap = CacheMap(userId, Map(displayKey -> Json.toJson(declaration)))

      when(
        mockCache.cache(eqTo(userId), eqTo(displayKey), eqTo(declaration))(using any(), any(), any())
      ).thenReturn(Future.successful(cacheMap))

      service.saveDisplayData4Later(userId, formId, declaration).map { result =>
        result shouldBe Some(declaration)
      }
    }
  }

  "Save4LaterService.fetchLastUpdateTime" should {
    "return the cached last update time" in {
      val userId = "user-999"
      val timestamp = 1699999999999L

      stubFetch(userId, Map(Save4LaterKeys.userLastTimeSavedKey -> Json.toJson(timestamp)))

      service.fetchLastUpdateTime(userId).map { result =>
        result shouldBe Some(timestamp)
      }
    }
  }

  "Save4LaterService.removeUserData" should {
    "remove all data for the given user" in {
      val userId = "user-to-delete"

      when(mockSessionRepository.clear(any[String])).thenReturn(Future.successful(true))
      when(mockCache.remove(any[String])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(()))

      service.removeUserData(userId).map { result =>
        result shouldBe ()
      }
    }

    "remove old cache even if session repository clear fails" in {
      val userId = "user-clear-fail"

      when(mockSessionRepository.clear(any[String]))
        .thenReturn(Future.failed(new RuntimeException("boom")))
      when(mockCache.remove(any[String])(using any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(()))

      service.removeUserData(userId).map { result =>
        result shouldBe ()
      }
    }
  }

  "Save4LaterService.fetch" should {
    "return data from session repository when enabled" in {
      when(mockAppConfig.isNewSessionRepositoryCacheEnabled).thenReturn(true)
      val userId = "user-fetch"
      val userAnswers = UserAnswers(userId, Map("key" -> Json.toJson("value")))

      when(mockSessionRepository.get(eqTo(userId))).thenReturn(Future.successful(Some(userAnswers)))

      service.fetch(userId).map { result =>
        result shouldBe Some(userAnswers)
      }
    }

    "fallback to short-lived cache and persist when session repository is empty" in {
      when(mockAppConfig.isNewSessionRepositoryCacheEnabled).thenReturn(true)
      val userId = "user-fallback"
      val cacheMap = CacheMap(userId, Map("key" -> Json.toJson("value")))

      when(mockSessionRepository.get(eqTo(userId))).thenReturn(Future.successful(None))
      when(mockCache.fetch(eqTo(userId))(using any(), any())).thenReturn(Future.successful(Some(cacheMap)))
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      service.fetch(userId).map { result =>
        result.map(_.data) shouldBe Some(cacheMap.data)
      }
    }

    "return old cache data even if session repository write fails" in {
      when(mockAppConfig.isNewSessionRepositoryCacheEnabled).thenReturn(true)
      val userId = "user-fallback-error"
      val cacheMap = CacheMap(userId, Map("key" -> Json.toJson("value")))

      when(mockSessionRepository.get(eqTo(userId))).thenReturn(Future.successful(None))
      when(mockCache.fetch(eqTo(userId))(using any(), any())).thenReturn(Future.successful(Some(cacheMap)))
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.failed(new RuntimeException("boom")))

      service.fetch(userId).map { result =>
        result.map(_.data) shouldBe Some(cacheMap.data)
      }
    }
  }

}
