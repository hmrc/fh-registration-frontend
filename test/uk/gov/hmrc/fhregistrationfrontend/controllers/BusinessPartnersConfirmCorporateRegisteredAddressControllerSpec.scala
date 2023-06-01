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
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class BusinessPartnersConfirmCorporateRegisteredAddressControllerSpec
    extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]

  val mockAppConfig = mock[FrontendAppConfig]

  val controller =
    new BusinessPartnersConfirmUnincorporatedRegisteredAddressController(
      commonDependencies,
      views,
      mockActions,
      mockAppConfig)(mockMcc)

  "load" should {
    "Render the confirm address page" when {
      "the new business partner pages are enabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        // should be mocked out when Save4Later changes included
        page.title() should include("Confirm the unincorporated body’s registered office address?")
        page.getElementsByTag("h1").text should include("Confirm the Test Corp’s registered office address")
        page.body.text should include("Test town")
        page.getElementById("confirm-edit").attr("href") should include("#")
        reset(mockActions)
      }
    }

    "return 200" when {
      "redirect to the next page" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.getElementsByClass("govuk-button").first.attr("href") should include("#")
        reset(mockActions)
      }
    }

    "Render the page not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        result.header.status shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("Page not found")
        reset(mockActions)
      }
    }
  }

  "next" should {
    "return 200" when {
      "the use clicks save and continue" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.next())(request))

        status(result) shouldBe OK
        contentAsString(result) shouldBe "Form submitted, with result:"
        reset(mockActions)
      }
    }

    "Render the page not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.next())(request))

        result.header.status shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("Page not found")
        reset(mockActions)
      }
    }
  }
}
