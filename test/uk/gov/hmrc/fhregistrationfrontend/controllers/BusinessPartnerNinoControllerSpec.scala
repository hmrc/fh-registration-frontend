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
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RadioHelper
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import models.{NormalMode, CheckMode}

class BusinessPartnerNinoControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]
  lazy val radioHelper = app.injector.instanceOf[RadioHelper]
  lazy val mockAppConfig = mock[FrontendAppConfig]
  val index = 1

  val controller =
    new BusinessPartnerNinoController(radioHelper, commonDependencies, views, mockActions, mockAppConfig)(mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>

    s"load when in $mode" should {
      "Render the business partner nino page" when {
        "The business partner v2 pages are enabled" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("Does the partner have a National Insurance number?")
          reset(mockActions)
        }
      }

      "render the not found page" when {
        "the new business partner pages are disabled" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe NOT_FOUND
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("Page not found")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" when {
      "The business partner v2 pages are enabled" should {
        "redirect to VAT number page" when {
          "the Yes radio button is selected and a NINO is entered" in {
            setupUserAction()

            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
            val request = FakeRequest()
              .withFormUrlEncodedBody(
                "nationalInsuranceNumber_yesNo" -> "true",
                "nationalInsuranceNumber_value" -> "QQ123456C"
              )
              .withMethod("POST")
            val result = await(csrfAddToken(controller.next(index, mode))(request))

            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get should include(routes.BusinessPartnersVatRegistrationNumberController.load().url)

            reset(mockActions)
          }
        }

        "return 400" when {
          "a radio button is not selected" in {
            setupUserAction()

            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
            val request = FakeRequest()
              .withFormUrlEncodedBody(
                "nationalInsuranceNumber_yesNo" -> "",
                "nationalInsuranceNumber_value" -> ""
              )
              .withMethod("POST")
            val result = await(csrfAddToken(controller.next(index, mode))(request))

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
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe NOT_FOUND
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("Page not found")
          reset(mockActions)
        }
      }
    }
  }
}
