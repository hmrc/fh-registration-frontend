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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.fhregistrationfrontend.services.AddressService
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.Future

class BusinessPartnersPartnershipRegisteredAddressControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockAddressService: AddressService = mock[AddressService]
  val controller =
    new BusinessPartnersPartnershipRegisteredAddressController(
      commonDependencies,
      views,
      mockActions,
      mockAppConfig,
      mockAddressService)(mockMcc)
  val chooseAddressUrl: String = routes.BusinessPartnersChooseAddressController.load().url
  val confirmPartnershipRegAddressUrl: String =
    routes.BusinessPartnersPartnershipConfirmRegisteredAddressController.load().url
  val cannotFindAddressUrl: String = routes.BusinessPartnersCannotFindAddressController.load().url

  "load" should {
    "Render the business partner address page" when {
      "the new business partner pages are enabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title should include("What is the partnership’s registered office address?")
        page.body.text() should include("What is Test User’s registered office address?")
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
      "redirect to the Choose Address page" when {
        "multiple addresses are found" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(
            mockAddressService
              .addressLookup(any(), any(), any())(any())
          ).thenReturn(
            Future.successful(
              Right(
                Map(
                  "123" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, Some("123")),
                  "234" -> Address("45 test lane", None, None, None, "SW1A 2AA", None, Some("234")))
              )
            )
          )

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("partnerAddressLine", ""),
              ("partnerPostcode", "SW1A 2AA")
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(chooseAddressUrl)
          reset(mockActions)
        }
      }

      "redirect to the Confirm Address page" when {
        "a single address is found" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(
            mockAddressService
              .addressLookup(any(), any(), any())(any())
          ).thenReturn(
            Future.successful(
              Right(
                Map(
                  "345" -> Address("1 Romford Road", None, None, None, "TF1 4ER", None, Some("345"))
                )
              )
            )
          )

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("partnerAddressLine", "1 Romford Road"),
              ("partnerPostcode", "TF1 4ER")
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(confirmPartnershipRegAddressUrl)
          reset(mockActions)
        }
      }

      "redirect to the Cannot Find Address page" when {
        "no addresses are found" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(
            mockAddressService
              .addressLookup(any(), any(), any())(any())
          ).thenReturn(
            Future.successful(
              Right(
                Map()
              )
            )
          )

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("partnerPostcode", "HR33 7GP"),
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(cannotFindAddressUrl)
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
