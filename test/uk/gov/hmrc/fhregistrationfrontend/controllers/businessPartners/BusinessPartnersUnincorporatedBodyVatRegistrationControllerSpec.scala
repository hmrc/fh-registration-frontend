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

package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import com.codahale.metrics.SharedMetricRegistries
import org.jsoup.Jsoup
import org.mockito.Mockito.{reset, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class BusinessPartnersUnincorporatedBodyVatRegistrationControllerSpec
    extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val controller =
    new BusinessPartnersUnincorporatedBodyVatRegistrationController(
      commonDependencies,
      views,
      mockActions,
      mockAppConfig)(mockMcc)

  val unincorpBodySaUtrUrl: String = routes.BusinessPartnersUnincorporatedBodyUtrController.load().url

  "load" should {
    "Render the businessPartnersCorporateBodyVatNumber page" when {
      "the new business partner pages are enabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = csrfAddToken(controller.load())(request)

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("Does the unincorporated body have a UK VAT registration number?")
        reset(mockActions)
      }
    }

    "Render the Not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
        val result = csrfAddToken(controller.load())(request)

        status(result) shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("Page not found")
        reset(mockActions)
      }
    }
  }

  "next" when {
    "the new business partner pages are enabled" should {
      "redirect to the Self Assessment UTR page" when {
        "the form has no errors, yes is selected and vatnumber supplied" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", "true"),
              ("vatNumber_value", "123456789")
            )
            .withMethod("POST")
          val result = csrfAddToken(controller.next())(request)

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(unincorpBodySaUtrUrl)
          reset(mockActions)
        }

        "the form has no errors and no is selected" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody("vatNumber_yesNo" -> "false")
            .withMethod("POST")
          val result = csrfAddToken(controller.next())(request)

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(unincorpBodySaUtrUrl)
          reset(mockActions)
        }

        "the user selects yes but doesn't enter a VAT number" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(("vatNumber_yesNo", "true"), ("vatNumber_value", ""))
            .withMethod("POST")
          val result = csrfAddToken(controller.next())(request)

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Does the unincorporated body have a UK VAT registration number?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
            "Enter the VAT registration number")
          reset(mockActions)
        }
      }
    }

    "Render the Not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
          .withFormUrlEncodedBody(("vatNumber_yesNo", "true"))
          .withMethod("POST")
        val result = csrfAddToken(controller.next())(request)

        status(result) shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("Page not found")
        reset(mockActions)
      }
    }
  }
}
