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
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, EmailVerificationConnectorMocks}
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class BusinessStatusControllerSpec
    extends ControllerSpecWithGuiceApp with EmailVerificationConnectorMocks with ActionsMock with BeforeAndAfterEach {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]
  lazy val mockAppConfig = mock[FrontendAppConfig]

  val controller = new BusinessStatusController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)

  "load" should {
    "Render the Business Status page" when {
      "The business partner v2 pages are enabled" in {
        setupUserAction()

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("Are you setting up a new fulfilment business?")
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
        page.title should include("Page not found")
        reset(mockActions)
      }
    }
  }

  "next" when {
    "The business partner v2 pages are enabled" should {
      "return 200" when {
        "Yes radio button selected and details are entered" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "isNewFulfilmentBusiness" -> "true",
              "proposedStartDate.day"   -> "01",
              "proposedStartDate.month" -> "01",
              "proposedStartDate.year"  -> "2020"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe OK
          contentAsString(result) shouldBe "Form submitted, with result: BusinessStatus(true,Some(2020-01-01))"
          reset(mockActions)
        }
      }

      "return 200" when {
        "No radio button is selected" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "isNewFulfilmentBusiness" -> "false"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe OK
          contentAsString(result) shouldBe "Form submitted, with result: BusinessStatus(false,None)"
          reset(mockActions)
        }
      }

      "return 400" when {
        "a radio button isn't selected" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "isNewFulfilmentBusiness" -> ""
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("Are you setting up a new fulfilment business?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
            "Select whether the business is setting up a new fulfilment business")
          reset(mockActions)
        }

        "Yes radio button is selected but no date is entered" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "isNewFulfilmentBusiness" -> "true",
              "proposedStartDate.day"   -> "",
              "proposedStartDate.month" -> "",
              "proposedStartDate.year"  -> ""
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("Are you setting up a new fulfilment business?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
            "Enter the intended start date")
          reset(mockActions)
        }

        "Yes radio button is selected but an invalid date is entered" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "isNewFulfilmentBusiness" -> "true",
              "proposedStartDate.day"   -> "ab",
              "proposedStartDate.month" -> "cd",
              "proposedStartDate.year"  -> "ef"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("Are you setting up a new fulfilment business?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
            "Enter a valid date for the intended start date")
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
        page.title should include("Page not found")
        reset(mockActions)
      }
    }
  }
}
