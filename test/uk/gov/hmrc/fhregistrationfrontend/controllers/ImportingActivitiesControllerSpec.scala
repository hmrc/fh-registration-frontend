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
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.EmailVerificationForm
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, EmailVerificationConnectorMocks, InMemoryShortLivedCache}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class ImportingActivitiesControllerSpec
    extends ControllerSpecWithGuiceApp with EmailVerificationConnectorMocks with ActionsMock with BeforeAndAfterEach {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]
  lazy val mockAppConfig = mock[FrontendAppConfig]

  val controller = new ImportingActivitiesController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)

  "load" should {
    "Render the importing activities page" when {
      "The business partner v2 pages are enabled" in {
        setupUserAction()

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = csrfAddToken(controller.load())(request)

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include(
          "Does the business have an EORI (Economic Operator Registration and Identification) number?")
        reset(mockActions)
      }
    }

    "render the not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
        val result = csrfAddToken(controller.load())(request)

        status(result) shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title should include("Page not found")
        reset(mockActions)
      }
    }
  }

  "next" when {
    "The business partner v2 pages are enabled" should {
      "return 200" when {
        "the importing activities form is filled out correctly" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "hasEori" -> "false"
            )
            .withMethod("POST")
          val result = csrfAddToken(controller.next())(request)

          status(result) shouldBe OK
          contentAsString(result) shouldBe "Form submitted, with result: ImportingActivities(false,None,None,None)"
          reset(mockActions)
        }
      }

      "return 400" when {
        "the importing activities form is filled out incorrectly" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody()
            .withMethod("POST")
          val result = csrfAddToken(controller.next())(request)

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title should include(
            "Does the business have an EORI (Economic Operator Registration and Identification) number?")
          reset(mockActions)
        }
      }
    }

    "the new business partner pages are disabled" should {
      "render the not found page" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
        val result = csrfAddToken(controller.next())(request)

        status(result) shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title should include("Page not found")
        reset(mockActions)
      }
    }
  }
}
