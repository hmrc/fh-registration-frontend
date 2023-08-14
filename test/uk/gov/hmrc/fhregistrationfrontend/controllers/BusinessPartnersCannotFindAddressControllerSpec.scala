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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class BusinessPartnersCannotFindAddressControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val controller =
    new BusinessPartnersCannotFindAddressController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)

  "load" should {
    "Render the Cannot Find Address page" when {
      "the new business partner pages are enabled" in {
        setupUserAction()

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("We cannot find any addresses for HR33 7GP")
        // should be mocked out when Save4Later changes included
        page.getElementById("enter-manually").attr("href") should include(
          "/fhdds/form/business-partners/enter-partnership-registered-office-address")
        reset(mockActions)
      }

      "the businessType returned is a limited-liability-partnership" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("We cannot find any addresses for HR33 7GP")
        // should be mocked out when Save4Later changes included
        page.getElementById("enter-manually").attr("href") should include(
          "/fhdds/form/business-partners/enter-partnership-registered-office-address")
        reset(mockActions)
      }

      "the businessType returned is a sole-proprietor" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("sole-proprietor")

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("We cannot find any addresses for HR33 7GP")
        // should be mocked out when Save4Later changes included
        page.getElementById("enter-manually").attr("href") should include(
          "/fhdds/form/business-partners/enter-partner-address")
        reset(mockActions)
      }

      "the businessType returned is a individual" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("individual")

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("We cannot find any addresses for HR33 7GP")
        // should be mocked out when Save4Later changes included
        page.getElementById("enter-manually").attr("href") should include(
          "/fhdds/form/business-partners/enter-partner-address")
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
}
