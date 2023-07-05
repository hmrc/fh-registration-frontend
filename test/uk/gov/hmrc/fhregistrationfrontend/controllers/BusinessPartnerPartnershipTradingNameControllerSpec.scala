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
import org.jsoup.Jsoup
import org.mockito.Mockito.{reset, when}
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class BusinessPartnerPartnershipTradingNameControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]
  lazy val mockAppConfig = mock[FrontendAppConfig]

  val pageTitle = "Does the partnership use a trading name that is different from its registered name?"

  val controller =
    new BusinessPartnerPartnershipTradingNameController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)

  "load" should {
    "Render the business partner trading name page" when {
      "The business partner v2 pages are enabled" in {
        setupUserAction()

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title should include(pageTitle)
        reset(mockActions)
      }
    }

    "render the not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title should include("Page not found")
        reset(mockActions)
      }
    }
  }

  "next" when {
    "The business partner v2 pages are enabled" should {
      "redirect to the partnership vat number page" when {
        "the businessType/legal entity of the partnership is a 'partnership'" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")
          val request = FakeRequest()
            .withCookies(Cookie("businessType", "partnership")) //TODO [DLS-7603] - temp save4later solution
            .withFormUrlEncodedBody(
              "tradingName_yesNo" -> "true",
              "tradingName_value" -> "new trading name"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include("/form/business-partners/partnership-vat-registration-number")
          reset(mockActions)
        }
      }

      "redirect to the partnership reg number page" when {
        "the businessType/legal entity of the partnership is a 'limited liability partnership'" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")
          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership")) //TODO [DLS-7603] - temp save4later solution
            .withFormUrlEncodedBody(
              "tradingName_yesNo" -> "true",
              "tradingName_value" -> "new trading name"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include("/form/business-partners/company-registration-number")
          reset(mockActions)
        }
      }

      "return 400 when the form containes errors" in {
        setupUserAction()

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")
        val request = FakeRequest()
          .withFormUrlEncodedBody(
            "tradingName_yesNo" -> "",
            "tradingName_value" -> ""
          )
          .withMethod("POST")
        val result = await(csrfAddToken(controller.next())(request))

        status(result) shouldBe BAD_REQUEST
        val page = Jsoup.parse(contentAsString(result))
        page.title should include(pageTitle)
        reset(mockActions)
      }
    }

    "the new business partner pages are disabled" should {
      "render the not found page" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.next())(request))

        status(result) shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title should include("Page not found")
        reset(mockActions)
      }
    }
  }
}
