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
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.connectors.AddressLookupErrorResponse
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.fhregistrationfrontend.services.AddressService
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.http.BadRequestException

class BusinessPartnersUnincorporatedBodyRegisteredAddressControllerSpec
    extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]

  val mockAppConfig = mock[FrontendAppConfig]

  val mockAddressService = mock[AddressService]

  val controller =
    new BusinessPartnersUnincorporatedBodyRegisteredAddressController(
      commonDependencies,
      views,
      mockActions,
      mockAppConfig,
      mockAddressService
    )(scala.concurrent.ExecutionContext.Implicits.global, mockMcc)

  "load" should {
    "Render the business partner address page" when {
      "the new business partner pages are enabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title should include("What is the unincorporated body’s registered office address?")
        page.body.text() should include("What is Test Unincorporated Body’s registered office address?")

        reset(mockActions)
      }
    }

    "Render the not found page" when {
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
    "the new business partner pages are enabled" should {
      "redirect to the choose-address page" when {
        "multiple results are found" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAddressService.addressLookup(any(), any(), any())(any)).thenReturn(
            Right(
              Map(
                "123" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, Some("123")),
                "234" -> Address("45 test lane", None, None, None, "SW1A 2AA", None, Some("234"))
              ))
          )
          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerPostcode", "AB1 2YZ"), ("partnerAddressLine", ""))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include("/fhdds/business-partners/choose-address")
          reset(mockActions)
        }
      }

      "redirect to the /confirm-unincorporated-body-registered-office-address page" when {
        "One result is found" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAddressService.addressLookup(any(), any(), any())(any)).thenReturn(
            Right(
              Map(
                "123" -> Address("44 test lane", None, None, None, "TF1 4ER", None, Some("123")),
              ))
          )
          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerPostcode", "TF1 4ER"), ("partnerAddressLine", "44 test lane"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            "/fhdds/form/business-partners/confirm-unincorporated-body-registered-office-address")
          reset(mockActions)
        }
      }

      "redirect to /business-partners/cannot-find-address page" when {
        "No match is found for postcode" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAddressService.addressLookup(any(), any(), any())(any)).thenReturn(Right(Map.empty[String, Address]))
          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerPostcode", "AB1 2YX"), ("partnerAddressLine", ""))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include("/fhdds/business-partners/cannot-find-address")
          reset(mockActions)
        }
      }

      "return a 400 BadRequest with error" when {
        "a AddressLookupErrorResponse is returned" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAddressService.addressLookup(any(), any(), any())(any))
            .thenReturn(Left(AddressLookupErrorResponse(new BadRequestException("unknown"))))
          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerPostcode", "AB1 2YX"), ("partnerAddressLine", ""))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe 400
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("What is the unincorporated body’s registered office address?")
          page.getElementsByClass("govuk-error-summary").text() should include(
            "There is a problem Sorry, there was problem performing this search, please try again and if the problem persists then enter the address manually")
          reset(mockActions)
        }
      }

      "Render the Not found page" when {
        "the new business partner pages are disabled" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerPostcode", "SW1A 2AA"))
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
