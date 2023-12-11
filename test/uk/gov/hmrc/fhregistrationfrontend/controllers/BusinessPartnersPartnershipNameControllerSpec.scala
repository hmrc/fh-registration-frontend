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

class BusinessPartnersPartnershipNameControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  lazy val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val controller =
    new BusinessPartnersPartnershipNameController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)
  val PartnershipNameTitle = "What is the name of the partnership? - Business partners"
  val pageNotFoundTitle = "Page not found"

  "load" should {
    "Render the business partner partnership name page" when {
      "The business partner v2 pages are enabled" in {
        setupUserAction()

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title should include(PartnershipNameTitle)
        reset(mockActions)
      }
    }

    "render the not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title should include(pageNotFoundTitle)
        reset(mockActions)
      }
    }
  }

  "next" should {
    "The business partner v2 pages are enabled" should {
      "return 303" when {
        "the form has no errors and the partnership name name is supplied" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "partnershipName" -> "Partnership name goes here"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          reset(mockActions)
        }
      }

      "return 400" when {
        "a partnership name isn't entered" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "partnershipName" -> ""
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe BAD_REQUEST
          reset(mockActions)
        }
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
        page.title should include(pageNotFoundTitle)
        reset(mockActions)
      }
    }
  }
}
