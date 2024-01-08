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
import org.mockito.Mockito.{reset, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class BusinessPartnersVatRegistrationNumberControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val index: Int = 1

  val controller =
    new BusinessPartnersVatRegistrationNumberController(
      commonDependencies,
      views,
      mockActions,
      mockAppConfig,
      mockSessionCache
    )(mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the businessPartnersVatRegistrationNumber page" when {
        "the new business partner pages are enabled and there is no page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)

          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load())(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Does the partner have a UK VAT registration number?")
          reset(mockActions)
        }
      }

      "Render the Not found page" when {
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

    "next" when {
      "the new business partner pages are enabled" should {
        "return 303" when {
          "the form has no errors, yes is selected and vatnumber supplied" in {
            setupUserAction()
            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
            val request = FakeRequest()
              .withFormUrlEncodedBody(("vatNumber_yesNo", "true"), ("vatNumber_value", "123456789"))
              .withMethod("POST")
            val result = await(csrfAddToken(controller.next())(request))

            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get should include("/fhdds/business-partners/partner-address")
            reset(mockActions)
          }

          "the form has no errors and no is selected" in {
            setupUserAction()
            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
            val request = FakeRequest()
              .withFormUrlEncodedBody("vatNumber_yesNo" -> "false")
              .withMethod("POST")
            val result = await(csrfAddToken(controller.next())(request))

            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get should include(
              "/fhdds/business-partners/partner-self-assessment-unique-taxpayer-reference")
            reset(mockActions)
          }
        }
      }

      "Render the Not found page" when {
        "the new business partner pages are disabled" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
          val request = FakeRequest()
            .withFormUrlEncodedBody(("chosenAddress", "1"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe NOT_FOUND
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Page not found")
          reset(mockActions)
        }
      }
    }
  }
}
