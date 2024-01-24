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

import com.codahale.metrics.SharedMetricRegistries
import models.{CheckMode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.models.TradingName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnershipTradingNamePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.Future

class BusinessPartnersPartnershipTradingNameControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  lazy val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  lazy val mockSessionCache: SessionRepository = mock[SessionRepository]

  val pageTitle = "Does the partnership use a trading name that is different from its registered name?"

  val controller =
    new BusinessPartnersPartnershipTradingNameController(commonDependencies, views, mockActions, mockAppConfig)(
      mockSessionCache)(mockMcc)
  val index = 1

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the business partner trading name page with no page data" in {
        val userAnswers = UserAnswers(testUserId)
        setupDataRequiredAction(userAnswers)

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load(index, mode))(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title should include(pageTitle)
        reset(mockActions)
      }

      "Render the business partner trading name page with page data" in {
        val tradingName = TradingName(true, Some("partner trading name"))
        val userAnswers = UserAnswers(testUserId)
          .set[TradingName](PartnershipTradingNamePage(index), tradingName)
          .success
          .value
        setupDataRequiredAction(userAnswers)

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load(index, mode))(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title should include(pageTitle)
        reset(mockActions)
      }
    }

    s"next when in $mode" should {
      "redirect to the partnership vat number page" when {
        "the businessType/legal entity of the partnership is a 'partnership'" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "partnership")) //TODO [DLS-7603] - temp save4later solution
            .withFormUrlEncodedBody(
              "tradingName_yesNo" -> "true",
              "tradingName_value" -> "new trading name"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            routes.BusinessPartnersPartnershipVatNumberController.load(index, mode).url)
          reset(mockActions)
        }
      }

      "redirect to the partnership reg number page" when {
        "the businessType/legal entity of the partnership is a 'limited liability partnership'" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership")) //TODO [DLS-7603] - temp save4later solution
            .withFormUrlEncodedBody(
              "tradingName_yesNo" -> "true",
              "tradingName_value" -> "new trading name"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load(index, mode).url)
          reset(mockActions)
        }
      }

      "redirect to the business partners page" when {
        "the businessType/legal entity of the partnership is a neither 'partnership' or 'limited liability partnership'" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType()).thenReturn("individual")
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "individual")) //TODO [DLS-7603] - temp save4later solution
            .withFormUrlEncodedBody(
              "tradingName_yesNo" -> "true",
              "tradingName_value" -> "new trading name"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(routes.BusinessPartnersController.load().url)
          reset(mockActions)
        }
      }

      "return 400 when the form contains errors" in {
        setupDataRequiredAction(UserAnswers(testUserId))

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")

        val request = FakeRequest()
          .withFormUrlEncodedBody(
            "tradingName_yesNo" -> "",
            "tradingName_value" -> ""
          )
          .withMethod("POST")
        val result = await(csrfAddToken(controller.next(index, mode))(request))

        status(result) shouldBe BAD_REQUEST
        val page = Jsoup.parse(contentAsString(result))
        page.title should include(pageTitle)
        reset(mockActions)
      }
    }
  }
}
