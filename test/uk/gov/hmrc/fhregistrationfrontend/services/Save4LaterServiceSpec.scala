package uk.gov.hmrc.fhregistrationfrontend.services

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.{Address, BusinessRegistrationDetails, Identification}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Save4LaterServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with ScalaFutures {

  given HeaderCarrier = HeaderCarrier()

  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val service = new Save4LaterService(mockCache)

  "Save4LaterService.saveBusinessType" should {
    "cache the business type and timestamp" in {
      val userId = "user-123"
      val businessType: BusinessType.Value = BusinessType.SoleTrader
      val formId = Save4LaterKeys.businessTypeKey

      val cacheMap = CacheMap(userId, Map(formId -> Json.toJson(businessType)))

      when(mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any()))
        .thenReturn(Future.successful(cacheMap))

      when(mockCache.cache(eqTo(userId), eqTo(formId), eqTo(businessType))(using any(), any(), any()))
        .thenReturn(Future.successful(cacheMap))

      service.saveBusinessType(userId, businessType).map { result =>
        result shouldBe Some(businessType)
      }
    }
  }

  "Save4LaterService.fetchBusinessType" should {
    "return the cached business type" in {
      val userId = "user-123"
      val formId = Save4LaterKeys.businessTypeKey
      val businessType = BusinessType.CorporateBody

      when(mockCache.fetchAndGetEntry[String](eqTo(userId), eqTo(formId))(using any(), any(), any()))
        .thenReturn(Future.successful(Some(businessType.toString)))

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

      when(mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any()))
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

      when(mockCache.fetchAndGetEntry[String](eqTo(userId), eqTo(Save4LaterKeys.verifiedEmailKey))(using any(), any(), any()))
        .thenReturn(Future.successful(Some(email)))

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

      when(mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.userLastTimeSavedKey), any[Long])(using any(), any(), any()))
        .thenReturn(Future.successful(cacheMap))

      when(mockCache.cache(eqTo(userId), eqTo(Save4LaterKeys.businessRegistrationDetailsKey), eqTo(brd))(using any(), any(), any()))
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

      when(mockCache.fetchAndGetEntry[BusinessRegistrationDetails](
        eqTo(userId),
        eqTo(Save4LaterKeys.businessRegistrationDetailsKey)
      )(using any(), any(), any()))
        .thenReturn(Future.successful(Some(brd)))

      service.fetchBusinessRegistrationDetails(userId).map { result =>
        result shouldBe Some(brd)
      }
    }
  }
}
